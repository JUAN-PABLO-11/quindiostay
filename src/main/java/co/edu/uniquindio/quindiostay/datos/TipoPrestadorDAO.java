package co.edu.uniquindio.quindiostay.datos;

import co.edu.uniquindio.quindiostay.modelo.TipoPrestador;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TipoPrestadorDAO {
    private static final String SELECT_BASE = "SELECT id_tipo_prestador, nombre FROM tipo_prestador";

    /** Lista los registros cuyo nombre contiene el filtro (vacío = todos). */
    public List<TipoPrestador> consultar(String filtro) throws SQLException {
        String patron = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + " WHERE LOWER(nombre) LIKE ? ORDER BY nombre")) {
            ps.setString(1, patron);
            List<TipoPrestador> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        }
    }

    /** Inserta y devuelve el id que generó Oracle (IDENTITY). */
    public int insertar(TipoPrestador tipoPrestador) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "INSERT INTO tipo_prestador (nombre) VALUES (?)", new String[] {"id_tipo_prestador"})) {
            ps.setString(1, tipoPrestador.nombre());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(TipoPrestador tipoPrestador) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("UPDATE tipo_prestador SET nombre = ? WHERE id_tipo_prestador = ?")) {
            ps.setString(1, tipoPrestador.nombre());
            ps.setInt(2, tipoPrestador.id());
            ps.executeUpdate();
        }
    }

    /** Si hay registros que lo usan, Oracle lanza ORA-02292 (RESTRICT). */
    public void eliminar(int id) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM tipo_prestador WHERE id_tipo_prestador = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private TipoPrestador mapear(ResultSet rs) throws SQLException {
        return new TipoPrestador(rs.getInt("id_tipo_prestador"), rs.getString("nombre"));
    }
}
