package co.edu.uniquindio.quindiostay.modelo;

/**
 * Quien inició sesión. Puede ser un usuario interno (USUARIO_SISTEMA) o un
 * cliente con cuenta (PERFIL_CLIENTE); en ese caso rol = "cliente".
 *
 * @param id          id_usuario o id_cliente según el caso
 * @param nombre      nombre que se muestra en la barra inferior
 * @param rol         nombre del rol (ver constantes en Sesion)
 * @param idPrestador solo para el encargado de prestador; si no, null
 */
public record Usuario(int id, String nombre, String rol, Integer idPrestador) {
}
