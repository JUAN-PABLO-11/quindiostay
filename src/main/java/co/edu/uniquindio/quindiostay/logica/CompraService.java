package co.edu.uniquindio.quindiostay.logica;

import co.edu.uniquindio.quindiostay.datos.CompraDAO;
import co.edu.uniquindio.quindiostay.modelo.DatosPago;
import co.edu.uniquindio.quindiostay.modelo.EstadoCuenta;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.modelo.PasajeroNuevo;
import co.edu.uniquindio.quindiostay.modelo.PlanVigente;
import co.edu.uniquindio.quindiostay.modelo.ResultadoPago;
import co.edu.uniquindio.quindiostay.modelo.SalidaDisponible;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Reserva y pago en línea del cliente.
 * Aquí van las reglas que Oracle no puede revisar fila por fila (RN15, RN19) y las
 * validaciones de la pasarela simulada. Oracle vuelve a revisar el resto con los triggers.
 */
public class CompraService {

    public static final List<String> METODOS_EN_LINEA = List.of("tarjeta de credito", "tarjeta debito", "pse");
    public static final List<String> TIPOS_DOCUMENTO_PASAJERO = List.of("CC", "TI", "RC", "CE", "pasaporte");
    public static final double ANTICIPO = 0.30;

    private final CompraDAO dao = new CompraDAO();
    private final SecureRandom azar = new SecureRandom();

    public List<Opcion> paquetes() throws SQLException {
        return dao.paquetesConSalidas();
    }

    public List<SalidaDisponible> salidas(int idPaquete) throws SQLException {
        return dao.salidas(idPaquete);
    }

    public List<PlanVigente> planes(int idPaquete, LocalDate fechaServicio) throws SQLException {
        return dao.planes(idPaquete, fechaServicio);
    }

    public Optional<PasajeroNuevo> yoComoPasajero() throws SQLException {
        return dao.clienteComoPasajero(Sesion.actual().id());
    }

    /**
     * Crea la reserva del cliente que inició sesión.
     * RN15: al menos un pasajero · RN16: cupos · RN19: valor = precio × pasajeros · RN30: un adulto.
     *
     * @return número de la reserva creada
     */
    public int reservar(SalidaDisponible salida, PlanVigente plan, List<PasajeroNuevo> pasajeros)
            throws SQLException, ReglaException {
        if (!Sesion.esCliente()) {
            throw new ReglaException("Solo un cliente con cuenta puede reservar en línea.");
        }
        if (salida == null || plan == null) {
            throw new ReglaException("Escoge la salida y el plan.");
        }
        if (pasajeros.isEmpty()) {
            throw new ReglaException("RN15: agrega al menos un pasajero.");
        }
        if (pasajeros.size() > salida.disponibles()) {
            throw new ReglaException("RN16: la salida solo tiene " + salida.disponibles() + " cupos libres.");
        }
        Set<String> documentos = new HashSet<>();
        for (PasajeroNuevo p : pasajeros) {
            if (!documentos.add(p.tipoDocumento() + p.numeroDocumento())) {
                throw new ReglaException("El pasajero con documento " + p.numeroDocumento() + " está repetido.");
            }
        }
        if (pasajeros.stream().noneMatch(p -> p.esAdultoEn(salida.fecha()))) {
            throw new ReglaException("RN30: un menor de edad no viaja sin al menos un adulto en la misma reserva.");
        }
        // Adultos primero: así el trigger de RN30 siempre encuentra al adulto antes que a los menores
        List<PasajeroNuevo> ordenados = new ArrayList<>(pasajeros);
        ordenados.sort(Comparator.comparing((PasajeroNuevo p) -> !p.esAdultoEn(salida.fecha())));

        long valorTotal = plan.precio() * pasajeros.size();   // RN19: se guarda y ya no cambia
        int idReserva = dao.crearReserva(Sesion.actual().id(), plan.id(), salida.id(), valorTotal, ordenados);
        try {
            dao.generarRecordatorios();   // RN32: recordatorio según el perfil del cliente
        } catch (SQLException e) {
            // el recordatorio no impide la reserva; se puede generar después desde el menú de operador
        }
        return idReserva;
    }

    /** Valida un pasajero antes de agregarlo a la lista. */
    public void validarPasajero(PasajeroNuevo p) throws ReglaException {
        if (p.nombres() == null || p.nombres().isBlank() || p.apellidos() == null || p.apellidos().isBlank()) {
            throw new ReglaException("Escribe los nombres y apellidos del pasajero.");
        }
        if (p.nombres().length() > 60 || p.apellidos().length() > 60) {
            throw new ReglaException("Nombres y apellidos: máximo 60 caracteres cada uno.");
        }
        if (!TIPOS_DOCUMENTO_PASAJERO.contains(p.tipoDocumento())) {
            throw new ReglaException("Selecciona el tipo de documento.");
        }
        if (p.numeroDocumento() == null || !p.numeroDocumento().matches("[A-Za-z0-9]{5,15}")) {
            throw new ReglaException("El documento debe tener de 5 a 15 letras o números, sin puntos ni espacios.");
        }
        if (p.fechaNacimiento() == null || p.fechaNacimiento().isAfter(LocalDate.now())
                || p.fechaNacimiento().isBefore(LocalDate.of(1900, 1, 1))) {
            throw new ReglaException("La fecha de nacimiento no es válida.");
        }
    }

    public EstadoCuenta estadoCuenta(int idReserva) throws SQLException, ReglaException {
        EstadoCuenta e = dao.estadoCuenta(idReserva)
                .orElseThrow(() -> new ReglaException("No existe la reserva " + idReserva + "."));
        if (Sesion.esCliente() && e.idCliente() != Sesion.actual().id()) {
            throw new ReglaException("Esa reserva no es tuya.");
        }
        return e;
    }

    /**
     * Pasarela de pago SIMULADA: valida los datos y aprueba o rechaza sin cobrar nada.
     * Para probar un rechazo se usa una tarjeta que termine en 0002.
     * El intento queda registrado (exitoso o fallido) con su referencia de transacción.
     */
    public ResultadoPago pagar(int idReserva, DatosPago d) throws SQLException, ReglaException {
        EstadoCuenta cuenta = estadoCuenta(idReserva);
        if (cuenta.estado().equals("cancelada") || cuenta.estado().equals("completada")) {
            throw new ReglaException("La reserva está " + cuenta.estado() + " y no recibe pagos.");
        }
        if (d.monto() <= 0) {
            throw new ReglaException("El monto debe ser mayor que cero.");
        }
        if (d.monto() > cuenta.saldo()) {   // RN21
            throw new ReglaException("RN21: el monto supera el saldo pendiente.");
        }
        if (!METODOS_EN_LINEA.contains(d.metodo())) {
            throw new ReglaException("En línea solo se paga con tarjeta o PSE.");
        }
        boolean aprobado;
        String prefijo;
        if (d.metodo().equals("pse")) {
            if (d.banco() == null || d.banco().isBlank()) {
                throw new ReglaException("Selecciona tu banco.");
            }
            aprobado = true;
            prefijo = "PSE";
        } else {
            String numero = d.numeroTarjeta() == null ? "" : d.numeroTarjeta().replaceAll("[ -]", "");
            if (!numero.matches("\\d{13,19}") || !luhn(numero)) {
                throw new ReglaException("El número de la tarjeta no es válido.");
            }
            if (d.titular() == null || d.titular().isBlank()) {
                throw new ReglaException("Escribe el nombre como aparece en la tarjeta.");
            }
            if (!vencimientoValido(d.vencimiento())) {
                throw new ReglaException("La fecha de vencimiento (MM/AA) no es válida o ya pasó.");
            }
            if (d.cvv() == null || !d.cvv().matches("\\d{3,4}")) {
                throw new ReglaException("El código de seguridad debe tener 3 o 4 dígitos.");
            }
            aprobado = !numero.endsWith("0002");   // simulación de rechazo del banco
            prefijo = d.metodo().equals("tarjeta de credito") ? "TC" : "TD";
        }
        String referencia = prefijo + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"))
                + "-" + String.format("%06X", azar.nextInt(0x1000000));
        int idPago = dao.registrarPago(idReserva, d.monto(), d.metodo(), aprobado, referencia);
        return new ResultadoPago(idPago, aprobado, referencia);
    }

    /** Algoritmo de Luhn: el dígito de control que tienen todas las tarjetas. */
    private static boolean luhn(String numero) {
        int suma = 0;
        boolean doble = false;
        for (int i = numero.length() - 1; i >= 0; i--) {
            int n = numero.charAt(i) - '0';
            if (doble) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            suma += n;
            doble = !doble;
        }
        return suma % 10 == 0;
    }

    private static boolean vencimientoValido(String texto) {
        if (texto == null || !texto.trim().matches("(0[1-9]|1[0-2])/\\d{2}")) {
            return false;
        }
        String[] partes = texto.trim().split("/");
        YearMonth vence = YearMonth.of(2000 + Integer.parseInt(partes[1]), Integer.parseInt(partes[0]));
        return !vence.isBefore(YearMonth.now());
    }

    /** Para el menú de operador: programa los recordatorios que falten. */
    public int generarRecordatorios() throws SQLException {
        return dao.generarRecordatorios();
    }
}
