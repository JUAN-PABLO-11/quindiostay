package co.edu.uniquindio.quindiostay.datos;

import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Busca las credenciales para el inicio de sesión. La contraseña se compara
 * en la capa de lógica (LoginService) con bcrypt; aquí solo se lee el hash.
 */
public class UsuarioDAO {

    /** Lo que se necesita para validar el login. */
    public record Credencial(Usuario usuario, String hash, String estado) {
    }

    /** Usuario interno (USUARIO_SISTEMA) por su nombre de usuario. */
    public Optional<Credencial> buscarInterno(String nombreUsuario) throws SQLException {
        String sql = """
                SELECT u.id_usuario, u.nombre_usuario, u.contrasena_cifrada, u.estado,
                       r.nombre AS rol, u.id_prestador
                  FROM usuario_sistema u
                  JOIN rol r ON r.id_rol = u.id_rol
                 WHERE u.nombre_usuario = ?
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                // getInt devuelve 0 si la columna es NULL; wasNull() justo después lo distingue
                int idPrestador = rs.getInt("id_prestador");
                Integer prestador = rs.wasNull() ? null : idPrestador;
                Usuario u = new Usuario(
                        rs.getInt("id_usuario"),
                        rs.getString("nombre_usuario"),
                        rs.getString("rol"),
                        prestador);
                return Optional.of(new Credencial(u, rs.getString("contrasena_cifrada"), rs.getString("estado")));
            }
        }
    }

    /** Cliente con cuenta (PERFIL_CLIENTE) por su correo. */
    public Optional<Credencial> buscarCliente(String correo) throws SQLException {
        String sql = """
                SELECT c.id_cliente, c.nombres || ' ' || c.apellidos AS nombre, pc.contrasena_cifrada
                  FROM cliente c
                  JOIN perfil_cliente pc ON pc.id_cliente = c.id_cliente
                 WHERE LOWER(c.correo) = LOWER(?)
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Usuario u = new Usuario(rs.getInt("id_cliente"), rs.getString("nombre"), Sesion.CLIENTE, null);
                return Optional.of(new Credencial(u, rs.getString("contrasena_cifrada"), "activo"));
            }
        }
    }
}
