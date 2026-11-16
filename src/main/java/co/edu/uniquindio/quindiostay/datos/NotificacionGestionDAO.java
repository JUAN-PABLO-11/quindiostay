package co.edu.uniquindio.quindiostay.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.quindiostay.modelo.NotificacionGestion;
import co.edu.uniquindio.quindiostay.modelo.Opcion;

/** Acceso a la tabla NOTIFICACION. */
public class NotificacionGestionDAO {
    private static final String SELECT_BASE = """
            SELECT n.id_notificacion, n.tipo, n.canal, n.mensaje, n.fecha_programada, n.fecha_envio,
                   n.estado, n.id_cliente, c.nombres || ' ' || c.apellidos AS cliente, n.id_reserva
              FROM notificacion n
              JOIN cliente c ON c.id_cliente = n.id_cliente
            """;

    /** Lista las notificaciones cuyo cliente, tipo, canal, estado o mensaje contiene el filtro (vacío = todas). */
    public List<NotificacionGestion> consultar(String filtro) throws SQLException {
        String sql = SELECT_BASE + """
                 WHERE LOWER(c.nombres || ' ' || c.apellidos) LIKE ? OR LOWER(n.tipo) LIKE ?
                    OR LOWER(n.canal) LIKE ? OR LOWER(n.estado) LIKE ? OR LOWER(n.mensaje) LIKE ?
                 ORDER BY n.fecha_programada DESC, n.id_notificacion DESC
                """;
        String patron = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, patron);
            }
            List<NotificacionGestion> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        }
    }

    /** Inserta y devuelve el id que generó Oracle (IDENTITY). */
    public int insertar(NotificacionGestion n) throws SQLException {
        String sql = """
                INSERT INTO notificacion (tipo, canal, mensaje, fecha_programada, fecha_envio, estado,
                                          id_cliente, id_reserva)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql, new String[] {"id_notificacion"})) {
            asignar(ps, n);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(NotificacionGestion n) throws SQLException {
        String sql = """
                UPDATE notificacion
                   SET tipo = ?, canal = ?, mensaje = ?, fecha_programada = ?, fecha_envio = ?, estado = ?,
                       id_cliente = ?, id_reserva = ?
                 WHERE id_notificacion = ?
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, n);
            ps.setInt(9, n.id());
            ps.executeUpdate();
        }
    }

    public void eliminar(int idNotificacion) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM notificacion WHERE id_notificacion = ?")) {
            ps.setInt(1, idNotificacion);
            ps.executeUpdate();
        }
    }

    /** Reservas de un cliente (la notificación y su reserva deben ser del mismo cliente: trigger R3). */
    public List<Opcion> reservasDeCliente(int idCliente) throws SQLException {
        String sql = """
                SELECT r.id_reserva,
                       '#' || r.id_reserva || ' · ' || pq.nombre || ' · salida ' || TO_CHAR(s.fecha_inicio, 'DD/MM/YYYY')
                  FROM reserva r
                  JOIN salida s   ON s.id_salida   = r.id_salida
                  JOIN paquete pq ON pq.id_paquete = s.id_paquete
                 WHERE r.id_cliente = ?
                 ORDER BY r.id_reserva DESC
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            List<Opcion> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Opcion(rs.getInt(1), rs.getString(2)));
                }
            }
            return lista;
        }
    }

    /** Parámetros 1 a 8, en el mismo orden del INSERT y del UPDATE. */
    private void asignar(PreparedStatement ps, NotificacionGestion n) throws SQLException {
        ps.setString(1, n.tipo());
        ps.setString(2, n.canal());
        ps.setString(3, n.mensaje());
        ps.setTimestamp(4, Timestamp.valueOf(n.fechaProgramada()));
        if (n.fechaEnvio() == null) {
            ps.setNull(5, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(5, Timestamp.valueOf(n.fechaEnvio()));
        }
        ps.setString(6, n.estado());
        ps.setInt(7, n.idCliente());
        if (n.idReserva() == null) {
            ps.setNull(8, Types.NUMERIC);
        } else {
            ps.setInt(8, n.idReserva());
        }
    }

    private NotificacionGestion mapear(ResultSet rs) throws SQLException {
        Timestamp envio = rs.getTimestamp("fecha_envio");
        int idReserva = rs.getInt("id_reserva");
        Integer reserva = rs.wasNull() ? null : idReserva;
        return new NotificacionGestion(
                rs.getInt("id_notificacion"),
                rs.getString("tipo"),
                rs.getString("canal"),
                rs.getString("mensaje"),
                rs.getTimestamp("fecha_programada").toLocalDateTime(),
                envio == null ? null : envio.toLocalDateTime(),
                rs.getString("estado"),
                rs.getInt("id_cliente"),
                rs.getString("cliente"),
                reserva);
    }
}
