package co.edu.uniquindio.quindiostay.modelo;

import java.time.LocalDate;

/**
 * @param id null cuando todavía no se ha guardado (es nuevo)
 */
public record Plan(
    Integer id,
        String nombre,
        String descripcion,
        String tipoPlan,
        int precio,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String estado,
        int idPaquete,
        String paquete
) {
    
}
