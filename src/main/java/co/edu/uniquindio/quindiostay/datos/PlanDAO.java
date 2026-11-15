package co.edu.uniquindio.quindiostay.datos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.quindiostay.modelo.Plan;

/** CRUD de la tabla PLAN*/
public class PlanDAO {
    private static final String SELECT_BASE = """
            SELECT pl.id_plan, pl.nombre, pl.descripcion, pl.tipo_plan, pl.precio,
                   pl.fecha_inicio_vigencia, pl.fecha_fin_vigencia, pl.estado,
                   pl.id_paquete, pq.nombre AS paquete
              FROM plan pl
              JOIN paquete pq ON pq.id_paquete = pl.id_paquete
            """;

    /**
     * Lista los planes cuyo nombre, paquete, tipo o estado contiene el filtro.
     * @param idPrestador si no es null, solo los planes de paquetes de ese prestador
     */
    public List<Plan> consultar(String filtro, Integer idPrestador) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE)
                .append(" WHERE (LOWER(pl.nombre) LIKE ? OR LOWER(pq.nombre) LIKE ?")
                .append(" OR LOWER(pl.tipo_plan) LIKE ? OR LOWER(pl.estado) LIKE ?)");
        if (idPrestador != null) {
            sql.append(" AND pq.id_prestador = ?");
        }
        sql.append(" ORDER BY pq.nombre, pl.precio");
        String patron = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 1; i <= 4; i++) {
                ps.setString(i, patron);
            }
            if (idPrestador != null) {
                ps.setInt(5, idPrestador);
            }
            List<Plan> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        }
    }

    /** Inserta y devuelve el id que generó Oracle (IDENTITY). */
    public int insertar(Plan p) throws SQLException {
        String sql = """
                INSERT INTO plan (nombre, descripcion, tipo_plan, precio, fecha_inicio_vigencia,
                                  fecha_fin_vigencia, estado, id_paquete)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql, new String[] {"id_plan"})) {
            asignar(ps, p);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(Plan p) throws SQLException {
        String sql = """
                UPDATE plan
                   SET nombre = ?, descripcion = ?, tipo_plan = ?, precio = ?, fecha_inicio_vigencia = ?,
                       fecha_fin_vigencia = ?, estado = ?, id_paquete = ?
                 WHERE id_plan = ?
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, p);
            ps.setInt(9, p.id());
            ps.executeUpdate();
        }
    }

    /** Si tiene reservas, Oracle lanza ORA-02292 (RESTRICT). */
    public void eliminar(int idPlan) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM plan WHERE id_plan = ?")) {
            ps.setInt(1, idPlan);
            ps.executeUpdate();
        }
    }

    /** Parámetros 1 a 8, en el mismo orden del INSERT y del UPDATE. */
    private void asignar(PreparedStatement ps, Plan p) throws SQLException {
        ps.setString(1, p.nombre());
        ps.setString(2, p.descripcion());
        ps.setString(3, p.tipoPlan());
        ps.setInt(4, p.precio());
        ps.setDate(5, Date.valueOf(p.fechaInicio()));
        ps.setDate(6, Date.valueOf(p.fechaFin()));
        ps.setString(7, p.estado());
        ps.setInt(8, p.idPaquete());
    }

    private Plan mapear(ResultSet rs) throws SQLException {
        return new Plan(
                rs.getInt("id_plan"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getString("tipo_plan"),
                rs.getInt("precio"),
                rs.getDate("fecha_inicio_vigencia").toLocalDate(),
                rs.getDate("fecha_fin_vigencia").toLocalDate(),
                rs.getString("estado"),
                rs.getInt("id_paquete"),
                rs.getString("paquete"));
    }
}
