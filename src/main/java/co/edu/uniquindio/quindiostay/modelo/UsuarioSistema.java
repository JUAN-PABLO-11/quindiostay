package co.edu.uniquindio.quindiostay.modelo;

/**
 * Se llama UsuarioSistema porque ya existe el record Usuario, que es quien inició sesión.
 * 
 * La contraseña cifrada NO va en este record ya que así el hash nunca viaja
 * hasta la pantalla. La clave se recibe aparte en UsuarioSistemaService.guardar().
 *
 * @param id          null cuando el usuario todavía no se ha guardado (es nuevo)
 * @param rol         nombre del rol que viene del JOIN
 * @param idPrestador solo para el rol "encargado de prestador" (RN03); de resto es null
 * @param prestador   nombre comercial del prestador (JOIN); null si no tiene
 */
public record UsuarioSistema(
    Integer id,
    String nombreUsuario,
    String estado,
    int idRol,
    String rol,
    Integer idPrestador,
    String prestador
) {
    
}
