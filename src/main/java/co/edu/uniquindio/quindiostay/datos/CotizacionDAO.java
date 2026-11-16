package co.edu.uniquindio.quindiostay.datos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import co.edu.uniquindio.quindiostay.modelo.Cotizacion;
import co.edu.uniquindio.quindiostay.modelo.DetalleCotizacion;

/**
 * Acceso a COTIZACION y a su DETALLE_COTIZACION. Guardar y actualizar escriben la
 * cotización y todas sus líneas en UNA transacción: se guarda todo o nada.
 */
public class CotizacionDAO {
    private static final String SELECT_BASE = """
            SELECT c.id_cotizacion, c.numero_cotizacion, c.fecha, c.valor_total, c.estado,
                   c.id_prestador, pr.nombre_comercial AS prestador,
                   c.id_cliente, cl.nombres || ' ' || cl.apellidos AS cliente
              FROM cotizacion c
              JOIN prestador pr ON pr.id_prestador = c.id_prestador
              JOIN cliente cl   ON cl.id_cliente   = c.id_cliente
            """;

    /**
     * Lista las cotizaciones cuyo número, cliente, prestador o estado contiene el filtro.
     *
     * @param idPrestador si no es null, solo las emitidas por ese prestador (encargado de prestador)
     */
    public List<Cotizacion> consultar(String filtro, Integer idPrestador) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE)
                .append(" WHERE (LOWER(c.numero_cotizacion) LIKE ? OR LOWER(cl.nombres || ' ' || cl.apellidos) LIKE ?")
                .append(" OR LOWER(pr.nombre_comercial) LIKE ? OR LOWER(c.estado) LIKE ?)");
        if (idPrestador != null) {
            sql.append(" AND c.id_prestador = ?");
        }
        sql.append(" ORDER BY c.fecha DESC, c.id_cotizacion DESC");
        String patron = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 1; i <= 4; i++) {
                ps.setString(i, patron);
            }
            if (idPrestador != null) {
                ps.setInt(5, idPrestador);
            }
            List<Cotizacion> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        }
    }

    /** Una cotización por id (vacío si no existe). Sirve para revisar permisos y estado actual. */
    public Optional<Cotizacion> buscar(int idCotizacion) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + " WHERE c.id_cotizacion = ?")) {
            ps.setInt(1, idCotizacion);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    /** Las líneas de una cotización, en orden. */
    public List<DetalleCotizacion> lineas(int idCotizacion) throws SQLException {
        String sql = """
                SELECT d.numero_linea, d.fecha_servicio, d.cantidad_personas, d.valor,
                       d.id_componente, c.nombre AS componente
                  FROM detalle_cotizacion d
                  JOIN componente c ON c.id_componente = d.id_componente
                 WHERE d.id_cotizacion = ?
                 ORDER BY d.numero_linea
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCotizacion);
            List<DetalleCotizacion> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new DetalleCotizacion(
                            rs.getInt("numero_linea"),
                            rs.getDate("fecha_servicio").toLocalDate(),
                            rs.getInt("cantidad_personas"),
                            rs.getInt("valor"),
                            rs.getInt("id_componente"),
                            rs.getString("componente")));
                }
            }
            return lista;
        }
    }

    /**
     * Inserta la cotización (con el siguiente número COT-xxxxxx) y todas sus líneas en una
     * sola transacción. Devuelve el id generado por Oracle.
     */
    public int insertar(Cotizacion c, List<DetalleCotizacion> lineas) throws SQLException {
        String sql = """
                INSERT INTO cotizacion (numero_cotizacion, fecha, valor_total, estado, id_prestador, id_cliente)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = cn.prepareStatement(sql, new String[] {"id_cotizacion"})) {
                    ps.setString(1, siguienteNumero(cn));
                    ps.setDate(2, Date.valueOf(c.fecha()));
                    ps.setLong(3, c.valorTotal());
                    ps.setString(4, c.estado());
                    ps.setInt(5, c.idPrestador());
                    ps.setInt(6, c.idCliente());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        id = rs.getInt(1);
                    }
                }
                insertarLineas(cn, id, lineas);
                cn.commit();
                return id;
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    /**
     * Actualiza la cotización y deja su detalle EXACTAMENTE como la lista recibida (borra las
     * líneas anteriores e inserta las nuevas), todo en una transacción. El prestador emisor
     * y el número no se modifican.
     */
    public void actualizar(Cotizacion c, List<DetalleCotizacion> lineas) throws SQLException {
        String sql = "UPDATE cotizacion SET fecha = ?, valor_total = ?, estado = ?, id_cliente = ? WHERE id_cotizacion = ?";
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setDate(1, Date.valueOf(c.fecha()));
                    ps.setLong(2, c.valorTotal());
                    ps.setString(3, c.estado());
                    ps.setInt(4, c.idCliente());
                    ps.setInt(5, c.id());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = cn.prepareStatement("DELETE FROM detalle_cotizacion WHERE id_cotizacion = ?")) {
                    ps.setInt(1, c.id());
                    ps.executeUpdate();
                }
                insertarLineas(cn, c.id(), lineas);
                cn.commit();
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    /** Las líneas se borran en cascada. Si ya se convirtió en reserva, Oracle lanza ORA-02292. */
    public void eliminar(int idCotizacion) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM cotizacion WHERE id_cotizacion = ?")) {
            ps.setInt(1, idCotizacion);
            ps.executeUpdate();
        }
    }

    /** Siguiente número disponible, con la forma COT-000136 (6 dígitos). */
    private String siguienteNumero(Connection cn) throws SQLException {
        String sql = "SELECT NVL(MAX(TO_NUMBER(SUBSTR(numero_cotizacion, 5))), 0) + 1 FROM cotizacion";
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return String.format("COT-%06d", rs.getLong(1));
        }
    }

    private void insertarLineas(Connection cn, int idCotizacion, List<DetalleCotizacion> lineas) throws SQLException {
        String sql = """
                INSERT INTO detalle_cotizacion (id_cotizacion, numero_linea, fecha_servicio,
                                                cantidad_personas, valor, id_componente)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (DetalleCotizacion d : lineas) {
                ps.setInt(1, idCotizacion);
                ps.setInt(2, d.numeroLinea());
                ps.setDate(3, Date.valueOf(d.fechaServicio()));
                ps.setInt(4, d.cantidadPersonas());
                ps.setInt(5, d.valor());
                ps.setInt(6, d.idComponente());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private Cotizacion mapear(ResultSet rs) throws SQLException {
        return new Cotizacion(
                rs.getInt("id_cotizacion"),
                rs.getString("numero_cotizacion").trim(),   // CHAR(10): sin espacios sobrantes
                rs.getDate("fecha").toLocalDate(),
                rs.getLong("valor_total"),
                rs.getString("estado"),
                rs.getInt("id_prestador"),
                rs.getString("prestador"),
                rs.getInt("id_cliente"),
                rs.getString("cliente"));
    }
}
