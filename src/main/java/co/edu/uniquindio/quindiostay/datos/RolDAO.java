package co.edu.uniquindio.quindiostay.datos;

import co.edu.uniquindio.quindiostay.modelo.Rol;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RolDAO {
    private static final String SELECT_BASE = "SELECT id_rol, nombre FROM rol";

    /** Lista los registros cuyo nombre contiene el filtro (vacío = todos). */
    public List<Rol> consultar(String filtro) throws SQLException {
        String patron = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + " WHERE LOWER(nombre) LIKE ? ORDER BY nombre")) {
            ps.setString(1, patron);
            List<Rol> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        }
    }

    /** Inserta y devuelve el id que generó Oracle (IDENTITY). */
    public int insertar(Rol rol) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "INSERT INTO rol (nombre) VALUES (?)", new String[] {"id_rol"})) {
            ps.setString(1, rol.nombre());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(Rol rol) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("UPDATE rol SET nombre = ? WHERE id_rol = ?")) {
            ps.setString(1, rol.nombre());
            ps.setInt(2, rol.id());
            ps.executeUpdate();
        }
    }

    /** Si hay registros que lo usan, Oracle lanza ORA-02292 (RESTRICT). */
    public void eliminar(int id) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM rol WHERE id_rol = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Nombre actual del rol en la base de datos (null si no existe). Sirve para proteger los roles del sistema. */
    public String nombrePorId(int id) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("SELECT nombre FROM rol WHERE id_rol = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private Rol mapear(ResultSet rs) throws SQLException {
        return new Rol(rs.getInt("id_rol"), rs.getString("nombre"));
    }   
}
