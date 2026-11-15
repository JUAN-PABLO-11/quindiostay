package co.edu.uniquindio.quindiostay.modelo;

/**
 * Fila de ITINERARIO_DIA (entidad débil de PAQUETE). 
 * Su PK es (idPaquete, numeroDia).
 */
public record ItinerarioDia(
    int idPaquete,
    String paquete,
    int numeroDia,
    String descripcion
) {
    
}
