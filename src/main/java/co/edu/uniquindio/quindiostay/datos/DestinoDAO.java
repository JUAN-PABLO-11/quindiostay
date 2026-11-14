package co.edu.uniquindio.quindiostay.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.quindiostay.modelo.Destino;

/** Acceso a la tabla DESTINO: consultar, insertar, actualizar y eliminar. */
public class DestinoDAO {
    private static final String SELECT_BASE = """
            SELECT d.id_destino, d.nombre, d.descripcion, d.id_municipio, m.nombre AS municipio
              FROM destino d
              JOIN municipio m ON m.id_municipio = d.id_municipio
            """;

    /** Lista los destinos cuyo nombre o municipio contiene el filtro (vacío = todos). */
    public List<Destino> consultar(String filtro) throws SQLException {
        String sql = SELECT_BASE + " WHERE LOWER(d.nombre) LIKE ? OR LOWER(m.nombre) LIKE ? ORDER BY d.nombre";
        String patron = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            List<Destino> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        }
    }

    /** Inserta y devuelve el id que generó Oracle (IDENTITY). */
    public int insertar(Destino d) throws SQLException {
        String sql = "INSERT INTO destino (nombre, descripcion, id_municipio) VALUES (?, ?, ?)";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql, new String[] {"id_destino"})) {
            asignar(ps, d);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(Destino d) throws SQLException {
        String sql = "UPDATE destino SET nombre = ?, descripcion = ?, id_municipio = ? WHERE id_destino = ?";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, d);
            ps.setInt(4, d.id());
            ps.executeUpdate();
        }
    }

    /** Si tiene paquetes, Oracle lanza ORA-02292 (RESTRICT). */
    public void eliminar(int idDestino) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM destino WHERE id_destino = ?")) {
            ps.setInt(1, idDestino);
            ps.executeUpdate();
        }
    }

    /** Parámetros 1 a 3, en el mismo orden del INSERT y del UPDATE. */
    private void asignar(PreparedStatement ps, Destino d) throws SQLException {
        ps.setString(1, d.nombre());
        if (d.descripcion() == null) {
            ps.setNull(2, Types.VARCHAR);
        } else {
            ps.setString(2, d.descripcion());
        }
        ps.setInt(3, d.idMunicipio());
    }

    private Destino mapear(ResultSet rs) throws SQLException {
        return new Destino(
                rs.getInt("id_destino"),
                rs.getString("nombre"),
                rs.getString("descripcion"),   // null si la columna es NULL
                rs.getInt("id_municipio"),
                rs.getString("municipio"));
    }
}
