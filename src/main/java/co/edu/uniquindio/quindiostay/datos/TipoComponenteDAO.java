package co.edu.uniquindio.quindiostay.datos;

import co.edu.uniquindio.quindiostay.modelo.TipoComponente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla TIPO_COMPONENTE: consultar, insertar, actualizar y eliminar. */
public class TipoComponenteDAO {
    private static final String SELECT_BASE = "SELECT id_tipo_componente, nombre FROM tipo_componente";

    /** Lista los registros cuyo nombre contiene el filtro (vacío = todos). */
    public List<TipoComponente> consultar(String filtro) throws SQLException {
        String patron = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + " WHERE LOWER(nombre) LIKE ? ORDER BY nombre")) {
            ps.setString(1, patron);
            List<TipoComponente> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        }
    }

    /** Inserta y devuelve el id que generó Oracle (IDENTITY). */
    public int insertar(TipoComponente tipoComponente) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "INSERT INTO tipo_componente (nombre) VALUES (?)", new String[] {"id_tipo_componente"})) {
            ps.setString(1, tipoComponente.nombre());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(TipoComponente tipoComponente) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("UPDATE tipo_componente SET nombre = ? WHERE id_tipo_componente = ?")) {
            ps.setString(1, tipoComponente.nombre());
            ps.setInt(2, tipoComponente.id());
            ps.executeUpdate();
        }
    }

    /** Si hay registros que lo usan, Oracle lanza ORA-02292 (RESTRICT). */
    public void eliminar(int id) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM tipo_componente WHERE id_tipo_componente = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private TipoComponente mapear(ResultSet rs) throws SQLException {
        return new TipoComponente(rs.getInt("id_tipo_componente"), rs.getString("nombre"));
    }
}
