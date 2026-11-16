package co.edu.uniquindio.quindiostay.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import co.edu.uniquindio.quindiostay.modelo.EstadoCuenta;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.modelo.Pago;

/**
 * Acceso a PAGO y PAGO_EN_LINEA. Crear y actualizar usan transacción.
 * el pago, su referencia de pasarela y el paso de la reserva de "pendiente" a "confirmada" se guardan juntos.
 */
public class PagoDAO {
    private static final String SELECT_BASE = """
            SELECT p.id_pago, p.fecha_pago, p.monto, p.metodo_pago, p.estado_pago, p.id_reserva,
                   '#' || p.id_reserva || ' · ' || c.nombres || ' ' || c.apellidos AS reserva,
                   pel.referencia_transaccion
              FROM pago p
              JOIN reserva r  ON r.id_reserva  = p.id_reserva
              JOIN cliente c  ON c.id_cliente  = r.id_cliente
              LEFT JOIN pago_en_linea pel ON pel.id_pago = p.id_pago
            """;

    /** Lista los pagos cuya reserva, cliente, método, estado o referencia contiene el filtro (vacío = todos). */
    public List<Pago> consultar(String filtro) throws SQLException {
        String sql = SELECT_BASE + """
                 WHERE TO_CHAR(p.id_reserva) LIKE ? OR LOWER(c.nombres || ' ' || c.apellidos) LIKE ?
                    OR LOWER(p.metodo_pago) LIKE ? OR LOWER(p.estado_pago) LIKE ?
                    OR LOWER(pel.referencia_transaccion) LIKE ?
                 ORDER BY p.fecha_pago DESC, p.id_pago DESC
                """;
        String patron = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, patron);
            }
            List<Pago> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        }
    }

    /** Un pago por id (vacío si no existe). */
    public Optional<Pago> buscar(int idPago) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + " WHERE p.id_pago = ?")) {
            ps.setInt(1, idPago);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    /**
     * Inserta el pago (la fecha la pone Oracle), su referencia si es en línea y, si quedó exitoso,
     * confirma la reserva que estaba pendiente. Todo en una transacción. Devuelve el id del pago.
     */
    public int insertar(Pago p) throws SQLException {
        String sql = "INSERT INTO pago (monto, metodo_pago, estado_pago, id_reserva) VALUES (?, ?, ?, ?)";
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                int idPago;
                try (PreparedStatement ps = cn.prepareStatement(sql, new String[] {"id_pago"})) {
                    ps.setLong(1, p.monto());
                    ps.setString(2, p.metodoPago());
                    ps.setString(3, p.estadoPago());
                    ps.setInt(4, p.idReserva());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        idPago = rs.getInt(1);
                    }
                }
                if (p.referencia() != null) {
                    try (PreparedStatement ps = cn.prepareStatement(
                            "INSERT INTO pago_en_linea (id_pago, referencia_transaccion) VALUES (?, ?)")) {
                        ps.setInt(1, idPago);
                        ps.setString(2, p.referencia());
                        ps.executeUpdate();
                    }
                }
                confirmarReservaSiCorresponde(cn, p.idReserva(), p.estadoPago());
                cn.commit();
                return idPago;
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    /**
     * Cambia el estado del pago y, en pagos en línea, su referencia. El monto, el método y la reserva
     * no se modifican (son el registro del dinero). Si pasa a exitoso, confirma la reserva pendiente.
     */
    public void actualizar(Pago p) throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = cn.prepareStatement("UPDATE pago SET estado_pago = ? WHERE id_pago = ?")) {
                    ps.setString(1, p.estadoPago());
                    ps.setInt(2, p.id());
                    ps.executeUpdate();
                }
                if (p.referencia() != null) {
                    try (PreparedStatement ps = cn.prepareStatement(
                            "UPDATE pago_en_linea SET referencia_transaccion = ? WHERE id_pago = ?")) {
                        ps.setString(1, p.referencia());
                        ps.setInt(2, p.id());
                        ps.executeUpdate();
                    }
                }
                confirmarReservaSiCorresponde(cn, p.idReserva(), p.estadoPago());
                cn.commit();
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    /** La referencia de pasarela se borra en cascada. */
    public void eliminar(int idPago) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM pago WHERE id_pago = ?")) {
            ps.setInt(1, idPago);
            ps.executeUpdate();
        }
    }

    /** Valor, pagado y estado de una reserva (vista v_pago_reserva). */
    public Optional<EstadoCuenta> cuenta(int idReserva) throws SQLException {
        String sql = "SELECT id_reserva, id_cliente, estado, valor_total, pagado FROM v_pago_reserva WHERE id_reserva = ?";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new EstadoCuenta(rs.getInt(1), rs.getInt(2), rs.getString(3),
                        rs.getLong(4), rs.getLong(5)));
            }
        }
    }

    /** Reservas que todavía reciben pagos (no canceladas ni completadas), con su saldo, para el combo. */
    public List<Opcion> reservasPagables() throws SQLException {
        String sql = """
                SELECT v.id_reserva,
                       '#' || v.id_reserva || ' · ' || c.nombres || ' ' || c.apellidos
                           || ' · saldo $' || TO_CHAR(v.saldo, 'FM999999999999')
                  FROM v_pago_reserva v
                  JOIN cliente c ON c.id_cliente = v.id_cliente
                 WHERE v.estado IN ('pendiente', 'confirmada')
                 ORDER BY v.id_reserva DESC
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Opcion> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new Opcion(rs.getInt(1), rs.getString(2)));
            }
            return lista;
        }
    }

    /** Un pago exitoso confirma la reserva que seguía pendiente (igual que el pago en línea del cliente). */
    private void confirmarReservaSiCorresponde(Connection cn, int idReserva, String estadoPago) throws SQLException {
        if (!"exitoso".equals(estadoPago)) {
            return;
        }
        try (PreparedStatement ps = cn.prepareStatement(
                "UPDATE reserva SET estado = 'confirmada' WHERE id_reserva = ? AND estado = 'pendiente'")) {
            ps.setInt(1, idReserva);
            ps.executeUpdate();
        }
    }

    private Pago mapear(ResultSet rs) throws SQLException {
        return new Pago(
                rs.getInt("id_pago"),
                rs.getTimestamp("fecha_pago").toLocalDateTime(),
                rs.getLong("monto"),
                rs.getString("metodo_pago"),
                rs.getString("estado_pago"),
                rs.getInt("id_reserva"),
                rs.getString("reserva"),
                rs.getString("referencia_transaccion"));
    }
}
