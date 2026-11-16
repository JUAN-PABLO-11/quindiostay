package co.edu.uniquindio.quindiostay.datos;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.modelo.PasajeroReserva;
import co.edu.uniquindio.quindiostay.modelo.PasajeroNuevo;
import co.edu.uniquindio.quindiostay.modelo.Reserva;

/**
 * Acceso a RESERVA y RESERVA_PASAJERO para el personal. 
 * Crear una reserva guarda los pasajeros y la reserva en UNA transacción. 
 * Cancelar NO es un UPDATE, llama al procedimiento cancelar_reserva, 
 * que calcula el reembolso (RN31) y avisa al cliente.
 */
public class ReservaDAO {
    private static final String SELECT_BASE = """
            SELECT r.id_reserva, r.fecha_creacion, r.valor_total, r.estado, r.canal,
                   r.id_plan, pl.nombre AS plan, r.id_salida, s.fecha_inicio, pq.nombre AS paquete,
                   r.id_cliente, c.nombres || ' ' || c.apellidos AS cliente,
                   (SELECT COUNT(*) FROM reserva_pasajero rp WHERE rp.id_reserva = r.id_reserva) AS pasajeros,
                   NVL((SELECT SUM(p.monto) FROM pago p
                         WHERE p.id_reserva = r.id_reserva AND p.estado_pago = 'exitoso'), 0) AS pagado,
                   r.fecha_cancelacion, r.valor_reembolso
              FROM reserva r
              JOIN plan pl    ON pl.id_plan      = r.id_plan
              JOIN salida s   ON s.id_salida     = r.id_salida
              JOIN paquete pq ON pq.id_paquete   = s.id_paquete
              JOIN cliente c  ON c.id_cliente    = r.id_cliente
            """;

    /** Lista las reservas cuyo número, cliente, paquete, estado o canal contiene el filtro (vacío = todas). */
    public List<Reserva> consultar(String filtro) throws SQLException {
        String sql = SELECT_BASE + """
                 WHERE TO_CHAR(r.id_reserva) LIKE ? OR LOWER(c.nombres || ' ' || c.apellidos) LIKE ?
                    OR LOWER(pq.nombre) LIKE ? OR LOWER(r.estado) LIKE ? OR LOWER(r.canal) LIKE ?
                 ORDER BY r.id_reserva DESC
                """;
        String patron = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, patron);
            }
            List<Reserva> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        }
    }

    /** Una reserva por id (vacío si no existe): estado actual y reembolso después de cancelar. */
    public Optional<Reserva> buscar(int idReserva) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + " WHERE r.id_reserva = ?")) {
            ps.setInt(1, idReserva);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    /**
     * Crea los pasajeros que no existan, la reserva y sus filas de RESERVA_PASAJERO en una
     * transacción. La lista debe venir con los ADULTOS PRIMERO: el trigger de RN30 revisa
     * después de cada inserción y un menor no puede quedar solo ni un instante.
     *
     * @return id de la reserva creada
     */
    public int insertar(int idCliente, String canal, int idPlan, int idSalida, long valorTotal,
                        List<PasajeroNuevo> pasajerosAdultosPrimero) throws SQLException {
        String sql = "INSERT INTO reserva (valor_total, canal, id_plan, id_salida, id_cliente) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                List<Integer> idsPasajeros = new ArrayList<>();
                for (PasajeroNuevo p : pasajerosAdultosPrimero) {
                    idsPasajeros.add(buscarOCrearPasajero(cn, p));
                }
                int idReserva;
                try (PreparedStatement ps = cn.prepareStatement(sql, new String[] {"id_reserva"})) {
                    ps.setLong(1, valorTotal);
                    ps.setString(2, canal);
                    ps.setInt(3, idPlan);
                    ps.setInt(4, idSalida);
                    ps.setInt(5, idCliente);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        idReserva = rs.getInt(1);
                    }
                }
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO reserva_pasajero (id_reserva, id_pasajero) VALUES (?, ?)")) {
                    for (int idPasajero : idsPasajeros) {   // uno por uno: el adulto entra primero
                        ps.setInt(1, idReserva);
                        ps.setInt(2, idPasajero);
                        ps.executeUpdate();
                    }
                }
                cn.commit();
                return idReserva;
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    /**
     * Cambia solo el canal y el estado (pendiente, confirmada o completada). El plan, la salida,
     * el cliente y el valor no se tocan: el valor se acordó al reservar. La cancelación va por cancelar().
     */
    public void actualizar(int idReserva, String canal, String estado) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("UPDATE reserva SET canal = ?, estado = ? WHERE id_reserva = ?")) {
            ps.setString(1, canal);
            ps.setString(2, estado);
            ps.setInt(3, idReserva);
            ps.executeUpdate();
        }
    }

    /**
     * Cancela con el procedimiento cancelar_reserva: calcula el reembolso (80 % de lo pagado si faltan
     * más de 5 días; 100 % si cancela la agencia), marca la reserva y deja la notificación al cliente.
     */
    public void cancelar(int idReserva, boolean porAgencia) throws SQLException {
        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall(
                     "BEGIN cancelar_reserva(p_id_reserva => ?, p_por_agencia => ?); END;")) {
            cs.setInt(1, idReserva);
            cs.setString(2, porAgencia ? "si" : "no");
            cs.execute();
        }
    }

    /** Las filas de RESERVA_PASAJERO se borran en cascada. RN29: el trigger no deja borrar una cancelada. */
    public void eliminar(int idReserva) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM reserva WHERE id_reserva = ?")) {
            ps.setInt(1, idReserva);
            ps.executeUpdate();
        }
    }

    /** Pasajeros de una reserva, con su edad el día de la salida. */
    public List<PasajeroReserva> pasajerosDe(int idReserva) throws SQLException {
        String sql = """
                SELECT p.nombres || ' ' || p.apellidos AS nombre,
                       p.tipo_documento || ' ' || p.numero_documento AS documento,
                       p.fecha_nacimiento,
                       TRUNC(MONTHS_BETWEEN(s.fecha_inicio, p.fecha_nacimiento) / 12) AS edad
                  FROM reserva_pasajero rp
                  JOIN reserva r  ON r.id_reserva  = rp.id_reserva
                  JOIN salida s   ON s.id_salida   = r.id_salida
                  JOIN pasajero p ON p.id_pasajero = rp.id_pasajero
                 WHERE rp.id_reserva = ?
                 ORDER BY p.fecha_nacimiento, p.apellidos
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            List<PasajeroReserva> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new PasajeroReserva(rs.getString("nombre"), rs.getString("documento"),
                            rs.getDate("fecha_nacimiento").toLocalDate(), rs.getInt("edad")));
                }
            }
            return lista;
        }
    }

    /** Todos los clientes con su documento (para el combo del formulario de reserva). */
    public List<Opcion> clientes() throws SQLException {
        String sql = """
                SELECT id_cliente, apellidos || ' ' || nombres || ' (' || tipo_documento || ' ' || numero_documento || ')'
                  FROM cliente
                 ORDER BY apellidos, nombres
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

    /** Si el pasajero ya existe (mismo tipo y número de documento) lo reutiliza; si no, lo crea. */
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

    private Reserva mapear(ResultSet rs) throws SQLException {
        Date cancelacion = rs.getDate("fecha_cancelacion");
        long reembolso = rs.getLong("valor_reembolso");
        Long valorReembolso = rs.wasNull() ? null : reembolso;
        return new Reserva(
                rs.getInt("id_reserva"),
                rs.getDate("fecha_creacion").toLocalDate(),
                rs.getLong("valor_total"),
                rs.getString("estado"),
                rs.getString("canal"),
                rs.getInt("id_plan"),
                rs.getString("plan"),
                rs.getInt("id_salida"),
                rs.getDate("fecha_inicio").toLocalDate(),
                rs.getString("paquete"),
                rs.getInt("id_cliente"),
                rs.getString("cliente"),
                rs.getInt("pasajeros"),
                rs.getLong("pagado"),
                cancelacion == null ? null : cancelacion.toLocalDate(),
                valorReembolso);
    }
}
