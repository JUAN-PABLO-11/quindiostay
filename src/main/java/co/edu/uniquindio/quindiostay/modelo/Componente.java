package co.edu.uniquindio.quindiostay.modelo;

/**
 * Fila de la tabla COMPONENTE. 
 * "tipoComponente" y "prestador" vienen del JOIN.
 * @param id          null cuando todavía no se ha guardado (es nuevo)
 * @param descripcion opcional: null si está vacía
 */
public record Componente(
        Integer id,
        String nombre,
        String descripcion,
        int costo,
        int idTipoComponente,
        String tipoComponente,
        int idPrestador,
        String prestador
) {
    
}
