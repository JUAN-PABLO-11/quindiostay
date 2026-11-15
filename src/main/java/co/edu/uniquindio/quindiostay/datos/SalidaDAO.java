package co.edu.uniquindio.quindiostay.datos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.quindiostay.modelo.Salida;

/** Acceso a la tabla SALIDA.*/
public class SalidaDAO {
    private static final String SELECT_BASE = """
            SELECT s.id_salida, s.fecha_inicio, s.cupo_maximo, s.estado, s.id_paquete,
                   pq.nombre AS paquete,
                   (SELECT COUNT(*)
                      FROM reserva r
                      JOIN reserva_pasajero rp ON rp.id_reserva = r.id_reserva
                     WHERE r.id_salida = s.id_salida AND r.estado <> 'cancelada') AS ocupados
              FROM salida s
              JOIN paquete pq ON pq.id_paquete = s.id_paquete
            """;

    /**
     * Lista las salidas cuyo paquete o estado contiene el filtro.
     * @param idPrestador si no es null, solo las salidas de paquetes de ese prestador
     */
    public List<Salida> consultar(String filtro, Integer idPrestador) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE)
                .append(" WHERE (LOWER(pq.nombre) LIKE ? OR LOWER(s.estado) LIKE ?")
                .append(" OR TO_CHAR(s.fecha_inicio, 'DD/MM/YYYY') LIKE ?)");
        if (idPrestador != null) {
            sql.append(" AND pq.id_prestador = ?");
        }
        sql.append(" ORDER BY s.fecha_inicio DESC, pq.nombre");
        String patron = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            if (idPrestador != null) {
                ps.setInt(4, idPrestador);
            }
            List<Salida> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Salida(
                            rs.getInt("id_salida"),
                            rs.getDate("fecha_inicio").toLocalDate(),
                            rs.getInt("cupo_maximo"),
                            rs.getString("estado"),
                            rs.getInt("id_paquete"),
                            rs.getString("paquete"),
                            rs.getInt("ocupados")));
                }
            }
            return lista;
        }
    }

    /** Inserta y devuelve el id que generó Oracle (IDENTITY). */
    public int insertar(Salida s) throws SQLException {
        String sql = "INSERT INTO salida (fecha_inicio, cupo_maximo, estado, id_paquete) VALUES (?, ?, ?, ?)";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql, new String[] {"id_salida"})) {
            ps.setDate(1, Date.valueOf(s.fechaInicio()));
            ps.setInt(2, s.cupoMaximo());
            ps.setString(3, s.estado());
            ps.setInt(4, s.idPaquete());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** El paquete de una salida no se cambia. Los triggers RN16 (cupo) y RN17 (confirmar) validan aquí. */
    public void actualizar(Salida s) throws SQLException {
        String sql = "UPDATE salida SET fecha_inicio = ?, cupo_maximo = ?, estado = ? WHERE id_salida = ?";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(s.fechaInicio()));
            ps.setInt(2, s.cupoMaximo());
            ps.setString(3, s.estado());
            ps.setInt(4, s.id());
            ps.executeUpdate();
        }
    }

    /** Si tiene reservas, Oracle lanza ORA-02292 (RESTRICT). */
    public void eliminar(int idSalida) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM salida WHERE id_salida = ?")) {
            ps.setInt(1, idSalida);
            ps.executeUpdate();
        }
    }
}
