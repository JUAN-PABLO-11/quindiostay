package co.edu.uniquindio.quindiostay.datos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.quindiostay.modelo.Cliente;

/** Acceso a la tabla CLIENTE.*/
public class ClienteDAO {
    private static final String SELECT_BASE = """
            SELECT id_cliente, nombres, apellidos, tipo_documento, numero_documento, correo,
                   telefono, fecha_nacimiento, ciudad_origen
              FROM cliente
            """;

    /** Lista los clientes cuyo nombre, documento, correo o ciudad contiene el filtro (vacío = todos). */
    public List<Cliente> consultar(String filtro) throws SQLException {
        String sql = SELECT_BASE + """
                 WHERE LOWER(nombres || ' ' || apellidos) LIKE ? OR LOWER(numero_documento) LIKE ?
                    OR LOWER(correo) LIKE ? OR LOWER(ciudad_origen) LIKE ?
                 ORDER BY apellidos, nombres
                """;
        String patron = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) {
                ps.setString(i, patron);
            }
            List<Cliente> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        }
    }

    /** Inserta y devuelve el id que generó Oracle (IDENTITY). */
    public int insertar(Cliente c) throws SQLException {
        String sql = """
                INSERT INTO cliente (nombres, apellidos, tipo_documento, numero_documento, correo,
                                     telefono, fecha_nacimiento, ciudad_origen)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql, new String[] {"id_cliente"})) {
            asignar(ps, c);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(Cliente c) throws SQLException {
        String sql = """
                UPDATE cliente
                   SET nombres = ?, apellidos = ?, tipo_documento = ?, numero_documento = ?, correo = ?,
                       telefono = ?, fecha_nacimiento = ?, ciudad_origen = ?
                 WHERE id_cliente = ?
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, c);
            ps.setInt(9, c.id());
            ps.executeUpdate();
        }
    }

    /**
     * Si tiene cotizaciones o reservas, Oracle lanza ORA-02292 (RESTRICT).
     * Su cuenta (PERFIL_CLIENTE) y sus notificaciones se borran en cascada.
     */
    public void eliminar(int idCliente) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM cliente WHERE id_cliente = ?")) {
            ps.setInt(1, idCliente);
            ps.executeUpdate();
        }
    }

    /** Parámetros 1 a 8, en el mismo orden del INSERT y del UPDATE. */
    private void asignar(PreparedStatement ps, Cliente c) throws SQLException {
        ps.setString(1, c.nombres());
        ps.setString(2, c.apellidos());
        ps.setString(3, c.tipoDocumento());
        ps.setString(4, c.numeroDocumento());
        ps.setString(5, c.correo());
        ps.setString(6, c.telefono());
        ps.setDate(7, Date.valueOf(c.fechaNacimiento()));
        ps.setString(8, c.ciudadOrigen());
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id_cliente"),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getString("tipo_documento"),
                rs.getString("numero_documento"),
                rs.getString("correo"),
                rs.getString("telefono"),
                rs.getDate("fecha_nacimiento").toLocalDate(),
                rs.getString("ciudad_origen"));
    }
}
