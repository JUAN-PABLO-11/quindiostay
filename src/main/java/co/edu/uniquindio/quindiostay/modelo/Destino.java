package co.edu.uniquindio.quindiostay.modelo;

/**
 * @param id          null cuando el destino todavía no se ha guardado (es nuevo)
 * @param descripcion opcional: null si está vacía
 */
public record Destino(
    Integer id,
    String nombre,
    String descripcion,
    int idMunicipio,
    String municipio
) {
    
}
