package co.edu.uniquindio.quindiostay.datos;

import co.edu.uniquindio.quindiostay.modelo.EstadoCuenta;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.modelo.PasajeroNuevo;
import co.edu.uniquindio.quindiostay.modelo.PlanVigente;
import co.edu.uniquindio.quindiostay.modelo.SalidaDisponible;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Compra en línea del cliente: catálogo de salidas y planes, crear la reserva con
 * sus pasajeros y registrar el pago. (El CRUD de reservas de los operadores es aparte.)
 */
public class CompraDAO {

    /** Paquetes públicos que tienen al menos una salida futura abierta. */
    public List<Opcion> paquetesConSalidas() throws SQLException {
        String sql = """
                SELECT p.id_paquete, p.nombre || '  ·  ' || d.nombre || ' (' || m.nombre || ')'
                  FROM paquete p
                  JOIN destino d   ON d.id_destino   = p.id_destino
                  JOIN municipio m ON m.id_municipio = d.id_municipio
                 WHERE p.visibilidad = 'publico'
                   AND EXISTS (SELECT 1 FROM salida s
                                WHERE s.id_paquete = p.id_paquete
                                  AND s.estado IN ('programada', 'confirmada')
                                  AND s.fecha_inicio > TRUNC(SYSDATE))
                 ORDER BY p.nombre
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

    /** Salidas futuras abiertas del paquete con sus cupos vendidos (reservas no canceladas). */
    public List<SalidaDisponible> salidas(int idPaquete) throws SQLException {
        String sql = """
                SELECT s.id_salida, s.fecha_inicio, s.cupo_maximo,
                       (SELECT COUNT(*) FROM reserva r
                          JOIN reserva_pasajero rp ON rp.id_reserva = r.id_reserva
                         WHERE r.id_salida = s.id_salida AND r.estado <> 'cancelada') AS vendidos
                  FROM salida s
                 WHERE s.id_paquete = ?
                   AND s.estado IN ('programada', 'confirmada')
                   AND s.fecha_inicio > TRUNC(SYSDATE)
                 ORDER BY s.fecha_inicio
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPaquete);
            List<SalidaDisponible> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new SalidaDisponible(rs.getInt(1), rs.getDate(2).toLocalDate(), rs.getInt(3), rs.getInt(4)));
                }
            }
            return lista;
        }
    }

    /** Planes activos y vigentes en la fecha del servicio (RN10). */
    public List<PlanVigente> planes(int idPaquete, LocalDate fechaServicio) throws SQLException {
        String sql = """
                SELECT id_plan, nombre, tipo_plan, precio, descripcion
                  FROM plan
                 WHERE id_paquete = ?
                   AND estado = 'activo'
                   AND ? BETWEEN fecha_inicio_vigencia AND fecha_fin_vigencia
                 ORDER BY precio
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPaquete);
            ps.setDate(2, Date.valueOf(fechaServicio));
            List<PlanVigente> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new PlanVigente(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getLong(4), rs.getString(5)));
                }
            }
            return lista;
        }
    }

    /** Los datos del propio cliente como pasajero (para "Agregarme como pasajero"). */
    public Optional<PasajeroNuevo> clienteComoPasajero(int idCliente) throws SQLException {
        String sql = "SELECT nombres, apellidos, tipo_documento, numero_documento, fecha_nacimiento FROM cliente WHERE id_cliente = ?";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new PasajeroNuevo(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getDate(5).toLocalDate()));
            }
        }
    }

    /**
     * Crea la reserva (pendiente, canal web) con sus pasajeros en UNA transacción.
     * Un pasajero que ya existe (mismo tipo y número de documento) se reutiliza: se registra una sola vez.
     * Los pasajeros deben venir ordenados con los adultos primero (RN30).
     */
    public int crearReserva(int idCliente, int idPlan, int idSalida, long valorTotal, List<PasajeroNuevo> pasajeros)
            throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                List<Integer> ids = new ArrayList<>();
                for (PasajeroNuevo p : pasajeros) {
                    ids.add(buscarOCrearPasajero(cn, p));
                }
                int idReserva;
                String sql = "INSERT INTO reserva (valor_total, canal, id_plan, id_salida, id_cliente) VALUES (?, 'web', ?, ?, ?)";
                try (PreparedStatement ps = cn.prepareStatement(sql, new String[] {"id_reserva"})) {
                    ps.setLong(1, valorTotal);
                    ps.setInt(2, idPlan);
                    ps.setInt(3, idSalida);
                    ps.setInt(4, idCliente);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        idReserva = rs.getInt(1);
                    }
                }
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO reserva_pasajero (id_reserva, id_pasajero) VALUES (?, ?)")) {
                    for (int idPasajero : ids) {   // uno por uno: el adulto entra primero
                        ps.setInt(1, idReserva);
                        ps.setInt(2, idPasajero);
                        ps.executeUpdate();
                    }
                }
                cn.commit();
                return idReserva;
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    private int buscarOCrearPasajero(Connection cn, PasajeroNuevo p) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT id_pasajero FROM pasajero WHERE tipo_documento = ? AND numero_documento = ?")) {
            ps.setString(1, p.tipoDocumento());
            ps.setString(2, p.numeroDocumento());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        String sql = """
                INSERT INTO pasajero (nombres, apellidos, tipo_documento, numero_documento, fecha_nacimiento)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = cn.prepareStatement(sql, new String[] {"id_pasajero"})) {
            ps.setString(1, p.nombres());
            ps.setString(2, p.apellidos());
            ps.setString(3, p.tipoDocumento());
            ps.setString(4, p.numeroDocumento());
            ps.setDate(5, Date.valueOf(p.fechaNacimiento()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Valor, pagado y estado de una reserva (vista v_pago_reserva). */
    public Optional<EstadoCuenta> estadoCuenta(int idReserva) throws SQLException {
        String sql = "SELECT id_reserva, id_cliente, estado, valor_total, pagado FROM v_pago_reserva WHERE id_reserva = ?";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new EstadoCuenta(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getLong(4), rs.getLong(5)));
            }
        }
    }

    /**
     * Registra el intento de pago en UNA transacción: el pago, su referencia de pasarela (pago en línea)
     * y, si fue aprobado, pasa la reserva de pendiente a confirmada y avisa al cliente por su canal.
     *
     * @return id del pago creado
     */
    public int registrarPago(int idReserva, long monto, String metodo, boolean aprobado, String referencia)
            throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                int idPago;
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO pago (monto, metodo_pago, estado_pago, id_reserva) VALUES (?, ?, ?, ?)",
                        new String[] {"id_pago"})) {
                    ps.setLong(1, monto);
                    ps.setString(2, metodo);
                    ps.setString(3, aprobado ? "exitoso" : "fallido");
                    ps.setInt(4, idReserva);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        idPago = rs.getInt(1);
                    }
                }
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO pago_en_linea (id_pago, referencia_transaccion) VALUES (?, ?)")) {
                    ps.setInt(1, idPago);
                    ps.setString(2, referencia);
                    ps.executeUpdate();
                }
                if (aprobado) {
                    int confirmadas;
                    try (PreparedStatement ps = cn.prepareStatement(
                            "UPDATE reserva SET estado = 'confirmada' WHERE id_reserva = ? AND estado = 'pendiente'")) {
                        ps.setInt(1, idReserva);
                        confirmadas = ps.executeUpdate();
                    }
                    notificar(cn, idReserva, "confirmacion de pago",
                            "Recibimos tu pago para la reserva " + idReserva + ". Referencia " + referencia + ".");
                    if (confirmadas == 1) {
                        notificar(cn, idReserva, "confirmacion de reserva",
                                "Tu reserva " + idReserva + " quedó confirmada. ¡Buen viaje!");
                    }
                }
                cn.commit();
                return idPago;
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    /** Notificación enviada al momento por el canal preferido del cliente (correo si no tiene cuenta). */
    private void notificar(Connection cn, int idReserva, String tipo, String mensaje) throws SQLException {
        String sql = """
                INSERT INTO notificacion (tipo, canal, mensaje, fecha_programada, fecha_envio, estado, id_cliente, id_reserva)
                SELECT ?, NVL(pc.canal_preferido, 'correo'), ?, SYSTIMESTAMP, SYSTIMESTAMP, 'enviada', r.id_cliente, r.id_reserva
                  FROM reserva r
                  LEFT JOIN perfil_cliente pc ON pc.id_cliente = r.id_cliente
                 WHERE r.id_reserva = ?
                """;
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, tipo);
            ps.setString(2, mensaje);
            ps.setInt(3, idReserva);
            ps.executeUpdate();
        }
    }

    /** Llama al procedimiento que programa los recordatorios según el perfil (RN32). */
    public int generarRecordatorios() throws SQLException {
        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call generar_recordatorios(?)}")) {
            cs.registerOutParameter(1, Types.INTEGER);
            cs.execute();
            return cs.getInt(1);
        }
    }
}
