package co.edu.uniquindio.quindiostay.logica;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

import co.edu.uniquindio.quindiostay.datos.PagoDAO;
import co.edu.uniquindio.quindiostay.modelo.EstadoCuenta;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.modelo.Pago;

/**
 * Pagos registrados por el personal (efectivo, transferencia, o tarjeta/PSE cobrados aparte).
 *
 * Reglas: monto mayor que 0, la reserva debe existir y seguir viva (RN23), la suma de los pagos
 * exitosos no supera el valor de la reserva (RN21), solo tarjeta y PSE llevan referencia de
 * transacción y la deben llevar (RN22). El monto, el método y la reserva de un pago no se editan:
 * un pago equivocado se marca "fallido" o se devuelve con "reembolsado", no se reescribe.
 */
public class PagoService {
    public static final List<String> METODOS =
            List.of("efectivo", "transferencia", "tarjeta de credito", "tarjeta debito", "pse");
    public static final List<String> METODOS_EN_LINEA = List.of("tarjeta de credito", "tarjeta debito", "pse");
    public static final List<String> ESTADOS_NUEVO = List.of("exitoso", "pendiente", "fallido");
    public static final List<String> ESTADOS_EDICION = List.of("exitoso", "pendiente", "fallido", "reembolsado");
    public static final long MONTO_MAXIMO = 1_000_000_000L;

    private static final Pattern REFERENCIA = Pattern.compile("^[A-Za-z0-9-]{1,40}$");

    private final PagoDAO dao = new PagoDAO();

    public List<Pago> consultar(String filtro) throws SQLException, ReglaException {
        exigirAcceso();
        return dao.consultar(filtro);
    }

    public List<Opcion> reservasPagables() throws SQLException, ReglaException {
        exigirAcceso();
        return dao.reservasPagables();
    }

    /** Saldo pendiente de una reserva (para proponer el monto en el formulario). */
    public long saldoDe(int idReserva) throws SQLException, ReglaException {
        exigirAcceso();
        return dao.cuenta(idReserva).map(EstadoCuenta::saldo).orElse(0L);
    }

    public static boolean esEnLinea(String metodo) {
        return METODOS_EN_LINEA.contains(metodo);
    }

    /** Inserta si es nuevo (id == null) o actualiza el estado/referencia si ya existe. */
    public void guardar(Pago p) throws SQLException, ReglaException {
        exigirAcceso();
        if (p.id() == null) {
            crear(p);
        } else {
            editar(p);
        }
    }

    private void crear(Pago p) throws SQLException, ReglaException {
        if (p.idReserva() <= 0) {
            throw new ReglaException("Selecciona la reserva.");
        }
        if (!METODOS.contains(p.metodoPago())) {
            throw new ReglaException("Selecciona el método de pago.");
        }
        if (!ESTADOS_NUEVO.contains(p.estadoPago())) {
            throw new ReglaException("Un pago nuevo queda exitoso, pendiente o fallido.");
        }
        if (p.monto() <= 0 || p.monto() > MONTO_MAXIMO) {
            throw new ReglaException("El monto debe ser mayor que 0 y no superar $1.000.000.000.");
        }
        validarReferencia(p);
        EstadoCuenta cuenta = dao.cuenta(p.idReserva())
                .orElseThrow(() -> new ReglaException("La reserva " + p.idReserva() + " no existe."));
        if ("cancelada".equals(cuenta.estado()) || "completada".equals(cuenta.estado())) {
            throw new ReglaException("RN23: la reserva está " + cuenta.estado() + " y no recibe pagos nuevos.");
        }
        if ("exitoso".equals(p.estadoPago()) && p.monto() > cuenta.saldo()) {
            throw new ReglaException("RN21: el pago supera el saldo pendiente de la reserva ($" + cuenta.saldo() + ").");
        }
        try {
            dao.insertar(p);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {   // UNIQUE: referencia de transacción repetida
                throw new ReglaException("Ya existe un pago con esa referencia de transacción.");
            }
            throw e;
        }
    }

    private void editar(Pago p) throws SQLException, ReglaException {
        Pago actual = dao.buscar(p.id()).orElseThrow(() -> new ReglaException("El pago ya no existe."));
        if (!ESTADOS_EDICION.contains(p.estadoPago())) {
            throw new ReglaException("Selecciona el estado del pago.");
        }
        if ("reembolsado".equals(actual.estadoPago()) && !"reembolsado".equals(p.estadoPago())) {
            throw new ReglaException("Un pago reembolsado ya no cambia de estado.");
        }
        if ("reembolsado".equals(p.estadoPago()) && !"exitoso".equals(actual.estadoPago())
                && !"reembolsado".equals(actual.estadoPago())) {
            throw new ReglaException("Solo se reembolsa un pago que estuvo exitoso.");
        }
        // Monto, método y reserva no se editan: se toman del registro guardado
        Pago nuevo = new Pago(actual.id(), actual.fechaPago(), actual.monto(), actual.metodoPago(),
                p.estadoPago(), actual.idReserva(), actual.reserva(), esEnLinea(actual.metodoPago()) ? p.referencia() : null);
        validarReferencia(nuevo);
        if ("exitoso".equals(nuevo.estadoPago()) && !"exitoso".equals(actual.estadoPago())) {
            EstadoCuenta cuenta = dao.cuenta(actual.idReserva())
                    .orElseThrow(() -> new ReglaException("La reserva " + actual.idReserva() + " no existe."));
            if ("cancelada".equals(cuenta.estado())) {
                throw new ReglaException("RN23: la reserva está cancelada y no recibe pagos nuevos.");
            }
            if (actual.monto() > cuenta.saldo()) {
                throw new ReglaException("RN21: el pago supera el saldo pendiente de la reserva ($" + cuenta.saldo() + ").");
            }
        }
        try {
            dao.actualizar(nuevo);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                throw new ReglaException("Ya existe un pago con esa referencia de transacción.");
            }
            throw e;
        }
    }

    /** Solo se borran pagos que no movieron dinero (pendientes o fallidos). */
    public void eliminar(Pago p) throws SQLException, ReglaException {
        exigirAcceso();
        if ("exitoso".equals(p.estadoPago()) || "reembolsado".equals(p.estadoPago())) {
            throw new ReglaException("Un pago " + p.estadoPago() + " es parte del historial de cobros y no se borra. "
                    + "Si fue un error, cámbiale el estado (fallido o reembolsado).");
        }
        dao.eliminar(p.id());
    }

    /** RN22: tarjeta y PSE llevan referencia de transacción; efectivo y transferencia no. */
    private void validarReferencia(Pago p) throws ReglaException {
        boolean tiene = p.referencia() != null && !p.referencia().isBlank();
        if (esEnLinea(p.metodoPago())) {
            if (!tiene || !REFERENCIA.matcher(p.referencia()).matches()) {
                throw new ReglaException("RN22: un pago por " + p.metodoPago() + " necesita la referencia de la "
                        + "transacción (letras, números y guion, máximo 40).");
            }
        } else if (tiene) {
            throw new ReglaException("RN22: un pago por " + p.metodoPago()
                    + " no es un pago en línea y no lleva referencia de transacción.");
        }
    }

    private void exigirAcceso() throws ReglaException {
        if (!Sesion.esAdmin() && !Sesion.esOperador()) {
            throw new ReglaException("Tu rol no tiene acceso a la gestión de pagos.");
        }
    }
}
