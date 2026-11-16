package co.edu.uniquindio.quindiostay.logica;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import co.edu.uniquindio.quindiostay.datos.NotificacionGestionDAO;
import co.edu.uniquindio.quindiostay.datos.ReservaDAO;
import co.edu.uniquindio.quindiostay.modelo.NotificacionGestion;
import co.edu.uniquindio.quindiostay.modelo.Opcion;

/**
 * Revisa el mensaje (1 a 500 caracteres), las listas de tipo, canal y estado, 
 * y que la fecha de envío sea coherente con el estado. RN06 (sin cuenta solo por correo), 
 * RN07 (promociones solo a quien las aceptó) y R3 (la reserva es del mismo cliente) las
 * revisa el trigger de Oracle y su mensaje llega a la pantalla. Solo administrador y operador.
 */
public class NotificacionGestionService {
    public static final List<String> TIPOS = List.of("recordatorio de reserva", "confirmacion de reserva",
            "confirmacion de pago", "cambio de estado", "promocion");
    public static final List<String> CANALES = List.of("correo", "sms", "whatsapp");
    public static final List<String> ESTADOS = List.of("pendiente", "enviada", "fallida", "leida");
    public static final int MAX_MENSAJE = 500;

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NotificacionGestionDAO dao = new NotificacionGestionDAO();

    public List<NotificacionGestion> consultar(String filtro) throws SQLException, ReglaException {
        exigirAcceso();
        return dao.consultar(filtro);
    }

    public List<Opcion> clientes() throws SQLException, ReglaException {
        exigirAcceso();
        return new ReservaDAO().clientes();
    }

    public List<Opcion> reservasDeCliente(int idCliente) throws SQLException, ReglaException {
        exigirAcceso();
        return dao.reservasDeCliente(idCliente);
    }

    /** Inserta si es nueva (id == null) o actualiza si ya existe. */
    public void guardar(NotificacionGestion n) throws SQLException, ReglaException {
        exigirAcceso();
        validar(n);
        NotificacionGestion lista = conFechaDeEnvio(n);
        if (lista.id() == null) {
            dao.insertar(lista);
        } else {
            dao.actualizar(lista);
        }
    }

    public void eliminar(NotificacionGestion n) throws SQLException, ReglaException {
        exigirAcceso();
        dao.eliminar(n.id());
    }

    private void validar(NotificacionGestion n) throws ReglaException {
        if (n.idCliente() <= 0) {
            throw new ReglaException("Selecciona el cliente.");
        }
        if (!TIPOS.contains(n.tipo())) {
            throw new ReglaException("Selecciona el tipo de notificación.");
        }
        if (!CANALES.contains(n.canal())) {
            throw new ReglaException("Selecciona el canal.");
        }
        if (!ESTADOS.contains(n.estado())) {
            throw new ReglaException("Selecciona el estado.");
        }
        if (n.mensaje() == null || n.mensaje().isBlank()) {
            throw new ReglaException("El mensaje es obligatorio.");
        }
        if (n.mensaje().length() > MAX_MENSAJE) {
            throw new ReglaException("El mensaje no puede tener más de " + MAX_MENSAJE + " caracteres (tiene "
                    + n.mensaje().length() + ").");
        }
        if (n.fechaProgramada() == null) {
            throw new ReglaException("La fecha programada es obligatoria.");
        }
    }

    /**
     * La fecha de envío debe ser vacía mientras está pendiente y no puede ser anterior a la programada.
     * Si se marca enviada o leída sin fecha de envío, se usa el momento actual.
     */
    private NotificacionGestion conFechaDeEnvio(NotificacionGestion n) throws ReglaException {
        LocalDateTime envio = n.fechaEnvio();
        switch (n.estado()) {
            case "pendiente" -> envio = null;
            case "enviada", "leida" -> {
                if (envio == null) {
                    envio = LocalDateTime.now();
                }
                if (envio.isBefore(n.fechaProgramada())) {
                    throw new ReglaException("No puede quedar " + n.estado() + " antes de su fecha programada ("
                            + n.fechaProgramada().format(FORMATO) + ").");
                }
            }
            default -> {
                if (envio != null && envio.isBefore(n.fechaProgramada())) {
                    envio = null;   // fallida con una fecha de envío incoherente: se deja vacía
                }
            }
        }
        return new NotificacionGestion(n.id(), n.tipo(), n.canal(), n.mensaje(), n.fechaProgramada(), envio,
                n.estado(), n.idCliente(), n.cliente(), n.idReserva());
    }

    private void exigirAcceso() throws ReglaException {
        if (!Sesion.esAdmin() && !Sesion.esOperador()) {
            throw new ReglaException("Tu rol no tiene acceso a la gestión de notificaciones.");
        }
    }
}
