package co.edu.uniquindio.quindiostay.datos;

import co.edu.uniquindio.quindiostay.modelo.RegistroCliente;
import co.edu.uniquindio.quindiostay.modelo.ReservaResumen;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cuenta del cliente: registro (CLIENTE + PERFIL_CLIENTE) y consulta de sus reservas.
 * Es distinto del CRUD de clientes que usan los operadores (ClienteDAO).
 */
public class CuentaDAO {

    /** Cliente que ya existe con un correo, para saber si ya tiene cuenta. */
    public record ClienteExistente(int id, String tipoDocumento, String numeroDocumento, boolean tienePerfil) {
    }

    public Optional<ClienteExistente> buscarPorCorreo(String correo) throws SQLException {
        String sql = """
                SELECT c.id_cliente, c.tipo_documento, c.numero_documento,
                       (SELECT COUNT(*) FROM perfil_cliente pc WHERE pc.id_cliente = c.id_cliente) AS perfiles
                  FROM cliente c
                 WHERE LOWER(c.correo) = LOWER(?)
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new ClienteExistente(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4) > 0));
            }
        }
    }

    /**
     * Registra un cliente nuevo con su cuenta, en una sola transacción:
     * se guardan los dos (CLIENTE y PERFIL_CLIENTE) o ninguno.
     */
    public int registrar(RegistroCliente r, String hash) throws SQLException {
        String sqlCliente = """
                INSERT INTO cliente (nombres, apellidos, tipo_documento, numero_documento, correo,
                                     telefono, fecha_nacimiento, ciudad_origen)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                int idCliente;
                try (PreparedStatement ps = cn.prepareStatement(sqlCliente, new String[] {"id_cliente"})) {
                    ps.setString(1, r.nombres());
                    ps.setString(2, r.apellidos());
                    ps.setString(3, r.tipoDocumento());
                    ps.setString(4, r.numeroDocumento());
                    ps.setString(5, r.correo());
                    ps.setString(6, r.telefono());
                    ps.setDate(7, Date.valueOf(r.fechaNacimiento()));
                    ps.setString(8, r.ciudadOrigen());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        idCliente = rs.getInt(1);
                    }
                }
                insertarPerfil(cn, idCliente, r, hash);
                cn.commit();
                return idCliente;
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    /** Crea solo la cuenta para un cliente que ya existía (compró por agencia o mostrador). */
    public void crearPerfil(int idCliente, RegistroCliente r, String hash) throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            insertarPerfil(cn, idCliente, r, hash);
        }
    }

    private void insertarPerfil(Connection cn, int idCliente, RegistroCliente r, String hash) throws SQLException {
        String sql = """
                INSERT INTO perfil_cliente (contrasena_cifrada, idioma, canal_preferido, dias_anticipacion,
                                            acepta_promociones, id_cliente)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, r.idioma());
            ps.setString(3, r.canalPreferido());
            ps.setInt(4, r.diasAnticipacion());
            ps.setString(5, r.aceptaPromociones() ? "si" : "no");
            ps.setInt(6, idCliente);
            ps.executeUpdate();
        }
    }

    /** Reservas de un cliente con lo pagado (solo pagos exitosos). */
    public List<ReservaResumen> reservasDe(int idCliente, String filtro) throws SQLException {
        String sql = """
                SELECT r.id_reserva, p.nombre AS paquete, pl.nombre AS plan, s.fecha_inicio, r.estado,
                       (SELECT COUNT(*) FROM reserva_pasajero rp WHERE rp.id_reserva = r.id_reserva) AS pasajeros,
                       r.valor_total,
                       NVL((SELECT SUM(pg.monto) FROM pago pg
                             WHERE pg.id_reserva = r.id_reserva AND pg.estado_pago = 'exitoso'), 0) AS pagado
                  FROM reserva r
                  JOIN salida s  ON s.id_salida  = r.id_salida
                  JOIN paquete p ON p.id_paquete = s.id_paquete
                  JOIN plan pl   ON pl.id_plan   = r.id_plan
                 WHERE r.id_cliente = ?
                   AND LOWER(p.nombre) LIKE ?
                 ORDER BY s.fecha_inicio DESC
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setString(2, "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%");
            List<ReservaResumen> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ReservaResumen(
                            rs.getInt("id_reserva"),
                            rs.getString("paquete"),
                            rs.getString("plan"),
                            rs.getDate("fecha_inicio").toLocalDate(),
                            rs.getString("estado"),
                            rs.getInt("pasajeros"),
                            rs.getLong("valor_total"),
                            rs.getLong("pagado")));
                }
            }
            return lista;
        }
    }
}
