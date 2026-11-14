package co.edu.uniquindio.quindiostay.modelo;

/**Entidad Municipio de la base de datos 
 * @param id         null cuando todavía no se ha guardado (es nuevo)
 * @param codigoDane código DANE de 5 dígitos (CHAR(5), único)
*/
public record Municipio(Integer id, String codigoDane, String nombre) {
}
