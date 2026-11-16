package co.edu.uniquindio.quindiostay.modelo;

import java.time.LocalDate;

/**
 * Una fila de la tabla RESERVA vista por el personal 
 * No confundir con ReservaResumen, que es lo que ve el cliente en "Mis reservas". 
 * Los campos de texto, "pasajeros" y "pagado" vienen de JOINs y conteos; no son columnas de RESERVA.
 * @param id             null cuando todavía no se ha guardado (es nueva)
 * @param valorTotal     valor acordado al reservar: precio del plan x pasajeros (RN19); no se recalcula
 * @param valorReembolso solo si está cancelada (RN31); null en las demás
 */
public record Reserva(
        Integer id,
        LocalDate fechaCreacion,
        long valorTotal,
        String estado,
        String canal,
        int idPlan,
        String plan,
        int idSalida,
        LocalDate fechaSalida,
        String paquete,
        int idCliente,
        String cliente,
        int pasajeros,
        long pagado,
        LocalDate fechaCancelacion,
        Long valorReembolso
) {
    public long saldo() {
        return valorTotal - pagado;
    }
}
