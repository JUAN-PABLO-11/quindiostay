package co.edu.uniquindio.quindiostay.modelo;

import java.time.LocalDate;

/**
 * "paquete" y "ocupados" no son columnas de SALIDA, vienen del JOIN 
 * y de un conteo (pasajeros de reservas no canceladas).
 * @param id       null cuando todavía no se ha guardado (es nuevo)
 * @param ocupados cupos ya vendidos (solo lectura; 0 si la salida es nueva)
 */
public record Salida(
        Integer id,
        LocalDate fechaInicio,
        int cupoMaximo,
        String estado,
        int idPaquete,
        String paquete,
        int ocupados
) {
    
}
