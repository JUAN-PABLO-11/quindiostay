package co.edu.uniquindio.quindiostay.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.quindiostay.modelo.UsuarioSistema;

public class UsuarioSistemaDAO {
    private static final String SELECT_BASE = """
            SELECT u.id_usuario, u.nombre_usuario, u.estado, u.id_rol, r.nombre AS rol,
                   u.id_prestador, p.nombre_comercial AS prestador
              FROM usuario_sistema u
              JOIN rol r            ON r.id_rol       = u.id_rol
              LEFT JOIN prestador p ON p.id_prestador = u.id_prestador
            """;

    /** Lista los usuarios cuyo nombre, rol, prestador o estado contiene el filtro (vacío = todos). */
    public List<UsuarioSistema> consultar(String filtro) throws SQLException {
        String sql = SELECT_BASE + """
                 WHERE LOWER(u.nombre_usuario) LIKE ? OR LOWER(r.nombre) LIKE ?
                    OR LOWER(p.nombre_comercial) LIKE ? OR LOWER(u.estado) LIKE ?
                 ORDER BY u.nombre_usuario
                """;
        String patron = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) {
                ps.setString(i, patron);
            }
            List<UsuarioSistema> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        }
    }

    /** Inserta con la contraseña ya cifrada y devuelve el id que generó Oracle (IDENTITY). */
    public int insertar(UsuarioSistema u, String hash) throws SQLException {
        String sql = """
                INSERT INTO usuario_sistema (nombre_usuario, contrasena_cifrada, estado, id_rol, id_prestador)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql, new String[] {"id_usuario"})) {
            ps.setString(1, u.nombreUsuario());
            ps.setString(2, hash);
            ps.setString(3, u.estado());
            ps.setInt(4, u.idRol());
            asignarPrestador(ps, 5, u.idPrestador());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * Si hash es null no se toca la contraseña
     */
    public void actualizar(UsuarioSistema u, String hash) throws SQLException {
        boolean cambiaClave = hash != null;
        String sql = cambiaClave
                ? """
                  UPDATE usuario_sistema
                     SET nombre_usuario = ?, estado = ?, id_rol = ?, id_prestador = ?, contrasena_cifrada = ?
                   WHERE id_usuario = ?
                  """
                : """
                  UPDATE usuario_sistema
                     SET nombre_usuario = ?, estado = ?, id_rol = ?, id_prestador = ?
                   WHERE id_usuario = ?
                  """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.nombreUsuario());
            ps.setString(2, u.estado());
            ps.setInt(3, u.idRol());
            asignarPrestador(ps, 4, u.idPrestador());
            if (cambiaClave) {
                ps.setString(5, hash);
                ps.setInt(6, u.id());
            } else {
                ps.setInt(5, u.id());
            }
            ps.executeUpdate();
        }
    }

    public void eliminar(int idUsuario) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM usuario_sistema WHERE id_usuario = ?")) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    /** id_prestador es opcional: null se guarda como NULL (setInt no acepta null). */
    private void asignarPrestador(PreparedStatement ps, int posicion, Integer idPrestador) throws SQLException {
        if (idPrestador == null) {
            ps.setNull(posicion, Types.NUMERIC);
        } else {
            ps.setInt(posicion, idPrestador);
        }
    }

    private UsuarioSistema mapear(ResultSet rs) throws SQLException {
        // getInt devuelve 0 si la columna es NULL; wasNull() justo después lo distingue
        int idPrestador = rs.getInt("id_prestador");
        Integer prestador = rs.wasNull() ? null : idPrestador;
        return new UsuarioSistema(
                rs.getInt("id_usuario"),
                rs.getString("nombre_usuario"),
                rs.getString("estado"),
                rs.getInt("id_rol"),
                rs.getString("rol"),
                prestador,
                rs.getString("prestador"));
    }
}
