package co.edu.uniquindio.quindiostay.modelo;

import java.time.LocalDateTime;

/** Notificación del historial del cliente. */
public record Notificacion(LocalDateTime programada, String tipo, String canal, String mensaje, String estado,
                           LocalDateTime enviada) {
}
