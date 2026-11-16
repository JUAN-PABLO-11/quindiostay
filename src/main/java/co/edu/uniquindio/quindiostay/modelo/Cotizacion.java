package co.edu.uniquindio.quindiostay.modelo;

import java.time.LocalDate;

/**
 * "prestador" y "cliente" vienen del JOIN.
 * El valor total NO se escribe a mano, es la suma de las líneas de su detalle.
 * @param id     null cuando todavía no se ha guardado (es nueva)
 * @param numero formato COT-000123; lo asigna el sistema al guardar (null si es nueva)
 */
public record Cotizacion(
        Integer id,
        String numero,
        LocalDate fecha,
        long valorTotal,
        String estado,
        int idPrestador,
        String prestador,
        int idCliente,
        String cliente
) {
    
}
