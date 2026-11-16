package co.edu.uniquindio.quindiostay.modelo;

import java.time.LocalDateTime;

/**
 * Una fila de la tabla PAGO (lo que en el negocio se cobra y se factura). "
 * reserva" y "referencia" vienen de JOINs 
 * La referencia de la pasarela solo existe en pagos con tarjeta o PSE (PAGO_EN_LINEA).
 * @param id         null cuando todavía no se ha guardado (es nuevo)
 * @param fechaPago  la pone Oracle al insertar (SYSDATE); null en un pago nuevo
 * @param referencia solo pagos en línea (RN22); null en efectivo y transferencia
 */
public record Pago(
        Integer id,
        LocalDateTime fechaPago,
        long monto,
        String metodoPago,
        String estadoPago,
        int idReserva,
        String reserva,
        String referencia
) {
    
}
