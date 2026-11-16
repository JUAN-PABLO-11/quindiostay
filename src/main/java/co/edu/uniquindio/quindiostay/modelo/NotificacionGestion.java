package co.edu.uniquindio.quindiostay.modelo;

import java.time.LocalDateTime;

/**
 * Una fila de la tabla NOTIFICACION vista por el personal 
 * No confundir con Notificacion, que es el historial que ve el cliente. 
 * "cliente" viene del JOIN.
 * @param id           null cuando todavía no se ha guardado (es nueva)
 * @param fechaEnvio   vacía mientras no se envía
 * @param idReserva    null en promociones y avisos sin reserva
 */
public record NotificacionGestion(
        Integer id,
        String tipo,
        String canal,
        String mensaje,
        LocalDateTime fechaProgramada,
        LocalDateTime fechaEnvio,
        String estado,
        int idCliente,
        String cliente,
        Integer idReserva
) {
    
}
