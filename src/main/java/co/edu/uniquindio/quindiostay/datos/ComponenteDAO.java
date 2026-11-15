package co.edu.uniquindio.quindiostay.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.quindiostay.modelo.Componente;

/** CRUD tabla COMPONENTE. */
public class ComponenteDAO {
    private static final String SELECT_BASE = """
            SELECT c.id_componente, c.nombre, c.descripcion, c.costo,
                   c.id_tipo_componente, t.nombre AS tipo_componente,
                   c.id_prestador, pr.nombre_comercial AS prestador
              FROM componente c
              JOIN tipo_componente t ON t.id_tipo_componente = c.id_tipo_componente
              JOIN prestador pr      ON pr.id_prestador      = c.id_prestador
            """;

    /**
     * Lista los componentes cuyo nombre, tipo o prestador contiene el filtro.
     * @param idPrestador si no es null, solo los de ese prestador (encargado de prestador)
     */
    public List<Componente> consultar(String filtro, Integer idPrestador) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE)
                .append(" WHERE (LOWER(c.nombre) LIKE ? OR LOWER(t.nombre) LIKE ? OR LOWER(pr.nombre_comercial) LIKE ?)");
        if (idPrestador != null) {
            sql.append(" AND c.id_prestador = ?");
        }
        sql.append(" ORDER BY c.nombre");
        String patron = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            if (idPrestador != null) {
                ps.setInt(4, idPrestador);
            }
            List<Componente> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        }
    }

    /** Inserta y devuelve el id que generó Oracle (IDENTITY). */
    public int insertar(Componente c) throws SQLException {
        String sql = """
                INSERT INTO componente (nombre, descripcion, costo, id_tipo_componente, id_prestador)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql, new String[] {"id_componente"})) {
            asignar(ps, c);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(Componente c) throws SQLException {
        String sql = """
                UPDATE componente
                   SET nombre = ?, descripcion = ?, costo = ?, id_tipo_componente = ?, id_prestador = ?
                 WHERE id_componente = ?
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, c);
            ps.setInt(6, c.id());
            ps.executeUpdate();
        }
    }

    /** Si está en un paquete o en una cotización, Oracle lanza ORA-02292 (RESTRICT). */
    public void eliminar(int idComponente) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM componente WHERE id_componente = ?")) {
            ps.setInt(1, idComponente);
            ps.executeUpdate();
        }
    }

    /** Parámetros 1 a 5, en el mismo orden del INSERT y del UPDATE. */
    private void asignar(PreparedStatement ps, Componente c) throws SQLException {
        ps.setString(1, c.nombre());
        if (c.descripcion() == null) {
            ps.setNull(2, Types.VARCHAR);
        } else {
            ps.setString(2, c.descripcion());
        }
        ps.setInt(3, c.costo());
        ps.setInt(4, c.idTipoComponente());
        ps.setInt(5, c.idPrestador());
    }

    private Componente mapear(ResultSet rs) throws SQLException {
        return new Componente(
                rs.getInt("id_componente"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getInt("costo"),
                rs.getInt("id_tipo_componente"),
                rs.getString("tipo_componente"),
                rs.getInt("id_prestador"),
                rs.getString("prestador"));
    }
}
