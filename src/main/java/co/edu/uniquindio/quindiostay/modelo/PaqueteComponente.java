package co.edu.uniquindio.quindiostay.modelo;

/**
 * Fila de la tabla PAQUETE_COMPONENTE (relación N:M "incluye"). 
 * Su llave es(idPaquete, idComponente). 
 * "paquete", "componente" y "tipoComponente" vienen del JOIN.
 */
public record PaqueteComponente(
        int idPaquete,
        String paquete,
        int idComponente,
        String componente,
        String tipoComponente
) {
    
}
