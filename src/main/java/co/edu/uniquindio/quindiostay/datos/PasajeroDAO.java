package co.edu.uniquindio.quindiostay.datos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.quindiostay.modelo.Pasajero;

/** Acceso a la tabla PASAJERO.*/
public class PasajeroDAO {
    private static final String SELECT_BASE = """
            SELECT id_pasajero, nombres, apellidos, tipo_documento, numero_documento, fecha_nacimiento
              FROM pasajero
            """;

    /** Lista los pasajeros cuyo nombre o documento contiene el filtro (vacío = todos). */
    public List<Pasajero> consultar(String filtro) throws SQLException {
        String sql = SELECT_BASE + """
                 WHERE LOWER(nombres || ' ' || apellidos) LIKE ? OR LOWER(numero_documento) LIKE ?
                 ORDER BY apellidos, nombres
                """;
        String patron = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            List<Pasajero> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        }
    }

    /** Inserta y devuelve el id que generó Oracle (IDENTITY). */
    public int insertar(Pasajero p) throws SQLException {
        String sql = """
                INSERT INTO pasajero (nombres, apellidos, tipo_documento, numero_documento, fecha_nacimiento)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql, new String[] {"id_pasajero"})) {
            asignar(ps, p);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(Pasajero p) throws SQLException {
        String sql = """
                UPDATE pasajero
                   SET nombres = ?, apellidos = ?, tipo_documento = ?, numero_documento = ?, fecha_nacimiento = ?
                 WHERE id_pasajero = ?
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, p);
            ps.setInt(6, p.id());
            ps.executeUpdate();
        }
    }

    /** Si viaja en alguna reserva, Oracle lanza ORA-02292 (RESTRICT). */
    public void eliminar(int idPasajero) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM pasajero WHERE id_pasajero = ?")) {
            ps.setInt(1, idPasajero);
            ps.executeUpdate();
        }
    }

    /** Parámetros 1 a 5, en el mismo orden del INSERT y del UPDATE. */
    private void asignar(PreparedStatement ps, Pasajero p) throws SQLException {
        ps.setString(1, p.nombres());
        ps.setString(2, p.apellidos());
        ps.setString(3, p.tipoDocumento());
        ps.setString(4, p.numeroDocumento());
        ps.setDate(5, Date.valueOf(p.fechaNacimiento()));
    }

    private Pasajero mapear(ResultSet rs) throws SQLException {
        return new Pasajero(
                rs.getInt("id_pasajero"),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getString("tipo_documento"),
                rs.getString("numero_documento"),
                rs.getDate("fecha_nacimiento").toLocalDate());
    }
}
