package co.edu.uniquindio.quindiostay.modelo;

/**
 * Una fila de la tabla PAQUETE.
 * Los campos "destino" y "prestador" no son columnas de PAQUETE: vienen del
 * JOIN y sirven para mostrar el nombre en la tabla en vez del id.
 *
 * @param id null cuando el paquete todavía no se ha guardado (es nuevo)
 */
public record Paquete(
        Integer id,
        String nombre,
        String descripcion,
        int duracionDias,
        String categoria,
        int minimoViajeros,
        String visibilidad,
        int idDestino,
        String destino,
        int idPrestador,
        String prestador) {
}
