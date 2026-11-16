package co.edu.uniquindio.quindiostay.logica;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.edu.uniquindio.quindiostay.datos.CatalogoDAO;
import co.edu.uniquindio.quindiostay.datos.CompraDAO;
import co.edu.uniquindio.quindiostay.datos.ReservaDAO;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.modelo.PasajeroReserva;
import co.edu.uniquindio.quindiostay.modelo.PasajeroNuevo;
import co.edu.uniquindio.quindiostay.modelo.PlanVigente;
import co.edu.uniquindio.quindiostay.modelo.Reserva;
import co.edu.uniquindio.quindiostay.modelo.SalidaDisponible;

/**
 * Reservas hechas por el personal (administrador u operador) en nombre de un cliente.
 *
 * Reglas presentes acá: RN15 (al menos un pasajero), RN16 (cupos), RN19 (valor = precio del plan x
 * pasajeros), RN30 (un menor no viaja sin un adulto; los adultos se guardan primero), RN28
 * (estados), RN29 (una cancelada no se borra) y RN31 (la cancelación va por el procedimiento
 * cancelar_reserva, nunca por UPDATE). Oracle vuelve a revisar todo con sus triggers.
 * La reserva que hace el cliente por su cuenta sigue en CompraService.
 */
public class ReservaService {
    public static final List<String> CANALES =
            List.of("mostrador", "telefono", "agencia intermediaria", "web", "aplicacion movil");
    /** Estados a los que se puede pasar editando: "cancelada" solo se alcanza con cancelar(). */
    public static final List<String> ESTADOS_EDITABLES = List.of("pendiente", "confirmada", "completada");
    public static final long VALOR_MAXIMO = 1_000_000_000L;

    private final ReservaDAO dao = new ReservaDAO();
    private final CompraService compra = new CompraService();

    public List<Reserva> consultar(String filtro) throws SQLException, ReglaException {
        exigirAcceso();
        return dao.consultar(filtro);
    }

    public List<PasajeroReserva> pasajerosDe(Reserva r) throws SQLException, ReglaException {
        exigirAcceso();
        return dao.pasajerosDe(r.id());
    }

    // ---- listas para el formulario ----

    public List<Opcion> clientes() throws SQLException, ReglaException {
        exigirAcceso();
        return dao.clientes();
    }

    public List<Opcion> paquetes() throws SQLException, ReglaException {
        exigirAcceso();
        return new CatalogoDAO().paquetes(null);
    }

    /** Salidas futuras abiertas del paquete (RN18) con sus cupos libres. */
    public List<SalidaDisponible> salidas(int idPaquete) throws SQLException, ReglaException {
        exigirAcceso();
        return new CompraDAO().salidas(idPaquete);
    }

    /** Planes activos y vigentes en la fecha de la salida (RN10). */
    public List<PlanVigente> planes(int idPaquete, LocalDate fechaSalida) throws SQLException, ReglaException {
        exigirAcceso();
        return new CompraDAO().planes(idPaquete, fechaSalida);
    }

    /** Valor que tendrá la reserva (RN19): precio del plan por número de pasajeros. */
    public static long valorDe(PlanVigente plan, int pasajeros) {
        return plan == null ? 0 : plan.precio() * pasajeros;
    }

    /** Valida un pasajero antes de agregarlo a la lista del formulario. */
    public void validarPasajero(PasajeroNuevo p) throws ReglaException {
        compra.validarPasajero(p);
    }

    // -------- operaciones -------- //

    /**
     * Crea una reserva para un cliente. Ordena los pasajeros con los adultos primero.
     * @return número de la reserva creada
     */
    public int crear(int idCliente, String canal, SalidaDisponible salida, PlanVigente plan,
                     List<PasajeroNuevo> pasajeros) throws SQLException, ReglaException {
        exigirAcceso();
        if (idCliente <= 0) {
            throw new ReglaException("Selecciona el cliente.");
        }
        if (!CANALES.contains(canal)) {
            throw new ReglaException("Selecciona el canal de la reserva.");
        }
        if (salida == null || plan == null) {
            throw new ReglaException("Escoge la salida y el plan.");
        }
        if (pasajeros == null || pasajeros.isEmpty()) {
            throw new ReglaException("RN15: agrega al menos un pasajero.");
        }
        if (pasajeros.size() > salida.disponibles()) {
            throw new ReglaException("RN16: la salida solo tiene " + salida.disponibles()
                    + (salida.disponibles() == 1 ? " cupo libre." : " cupos libres."));
        }
        Set<String> documentos = new HashSet<>();
        for (PasajeroNuevo p : pasajeros) {
            compra.validarPasajero(p);
            if (!documentos.add(p.tipoDocumento() + p.numeroDocumento())) {
                throw new ReglaException("El pasajero con documento " + p.numeroDocumento() + " está repetido.");
            }
        }
        if (pasajeros.stream().noneMatch(p -> p.esAdultoEn(salida.fecha()))) {
            throw new ReglaException("RN30: un menor de edad no viaja sin al menos un adulto en la misma reserva.");
        }
        long valor = valorDe(plan, pasajeros.size());
        if (valor <= 0 || valor > VALOR_MAXIMO) {
            throw new ReglaException("El valor de la reserva debe ser mayor que 0 y no superar $1.000.000.000.");
        }
        // Adultos primero: así el trigger de RN30 siempre encuentra al adulto antes que a los menores
        List<PasajeroNuevo> ordenados = new ArrayList<>(pasajeros);
        ordenados.sort(Comparator.comparing((PasajeroNuevo p) -> !p.esAdultoEn(salida.fecha())));

        int id = dao.insertar(idCliente, canal, plan.id(), salida.id(), valor, ordenados);
        try {
            compra.generarRecordatorios();   // RN32: el recordatorio no impide la reserva si falla
        } catch (SQLException e) {
            // se puede generar después desde Gestión > Generar recordatorios pendientes
        }
        return id;
    }

    /** Cambia el canal y/o el estado de una reserva viva (RN28). El valor y los pasajeros no se tocan. */
    public void actualizar(Reserva r, String canal, String estado) throws SQLException, ReglaException {
        exigirAcceso();
        if (!CANALES.contains(canal)) {
            throw new ReglaException("Selecciona el canal.");
        }
        if (!ESTADOS_EDITABLES.contains(estado)) {
            throw new ReglaException("Para cancelar una reserva usa el botón Cancelar reserva.");
        }
        Reserva actual = dao.buscar(r.id()).orElseThrow(() -> new ReglaException("La reserva ya no existe."));
        if (!actual.estado().equals(estado)) {
            if ("cancelada".equals(actual.estado()) || "completada".equals(actual.estado())) {
                throw new ReglaException("RN28: la reserva ya está " + actual.estado() + " y no puede cambiar de estado.");
            }
            if ("completada".equals(estado) && !"confirmada".equals(actual.estado())) {
                throw new ReglaException("RN28: una reserva solo pasa a completada si antes estuvo confirmada (está "
                        + actual.estado() + ").");
            }
        }
        dao.actualizar(r.id(), canal, estado);
    }

    /**
     * Cancela con el procedimiento cancelar_reserva (RN31), no con UPDATE.
     * @param porAgencia true si cancela la agencia (reembolso del 100 %); false si cancela el cliente
     *                   (80 % de lo pagado si faltan más de 5 días para la salida; si no, nada)
     * @return la reserva ya cancelada, con su reembolso calculado
     */
    public Reserva cancelar(Reserva r, boolean porAgencia) throws SQLException, ReglaException {
        exigirAcceso();
        Reserva actual = dao.buscar(r.id()).orElseThrow(() -> new ReglaException("La reserva ya no existe."));
        if ("cancelada".equals(actual.estado()) || "completada".equals(actual.estado())) {
            throw new ReglaException("RN31: la reserva " + r.id() + " ya está " + actual.estado() + ".");
        }
        dao.cancelar(r.id(), porAgencia);
        return dao.buscar(r.id()).orElse(actual);
    }

    public void eliminar(Reserva r) throws SQLException, ReglaException {
        exigirAcceso();
        if ("cancelada".equals(r.estado())) {
            throw new ReglaException("RN29: la reserva " + r.id() + " está cancelada y se conserva como historial.");
        }
        try {
            dao.eliminar(r.id());
        } catch (SQLException e) {
            if (e.getErrorCode() == 2292) {   // RESTRICT: tiene pagos
                throw new ReglaException("No se puede eliminar la reserva " + r.id()
                        + ": tiene pagos registrados (RESTRICT). Cancélala para conservar el historial.");
            }
            throw e;
        }
    }

    private void exigirAcceso() throws ReglaException {
        if (!Sesion.esAdmin() && !Sesion.esOperador()) {
            throw new ReglaException("Tu rol no tiene acceso a la gestión de reservas.");
        }
    }
}
