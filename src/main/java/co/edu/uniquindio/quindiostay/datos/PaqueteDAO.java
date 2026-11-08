package co.edu.uniquindio.quindiostay.datos;

import co.edu.uniquindio.quindiostay.modelo.Paquete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla PAQUETE: consultar, insertar, actualizar y eliminar.
 *
 * PLANTILLA: para otra tabla, copiar este archivo y cambiar
 *   1. el SELECT_BASE (columnas y JOIN para mostrar nombres en vez de ids),
 *   2. los INSERT / UPDATE / DELETE,
 *   3. el método mapear() (fila del ResultSet -> objeto del modelo),
 *   4. el método asignar() (objeto del modelo -> parámetros "?").
 * Siempre PreparedStatement con "?", nunca concatenar texto del usuario en el SQL.
 */
public class PaqueteDAO {

    private static final String SELECT_BASE = """
            SELECT p.id_paquete, p.nombre, p.descripcion, p.duracion_dias, p.categoria,
                   p.minimo_viajeros, p.visibilidad,
                   p.id_destino, d.nombre AS destino,
                   p.id_prestador, pr.nombre_comercial AS prestador
              FROM paquete p
              JOIN destino d    ON d.id_destino    = p.id_destino
              JOIN prestador pr ON pr.id_prestador = p.id_prestador
            """;

    /**
     * Lista los paquetes cuyo nombre, destino o categoría contiene el filtro.
     *
     * @param filtro      texto de búsqueda (vacío = todos)
     * @param idPrestador si no es null, solo los de ese prestador (encargado de prestador)
     */
    public List<Paquete> consultar(String filtro, Integer idPrestador) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE)
                .append(" WHERE (LOWER(p.nombre) LIKE ? OR LOWER(d.nombre) LIKE ? OR LOWER(p.categoria) LIKE ?)");
        if (idPrestador != null) {
            sql.append(" AND p.id_prestador = ?");
        }
        sql.append(" ORDER BY p.nombre");

        String patron = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            if (idPrestador != null) {
                ps.setInt(4, idPrestador);
            }
            List<Paquete> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        }
    }

    /** Inserta y devuelve el id que generó Oracle (IDENTITY). */
    public int insertar(Paquete p) throws SQLException {
        String sql = """
                INSERT INTO paquete (nombre, descripcion, duracion_dias, categoria, minimo_viajeros,
                                     visibilidad, id_destino, id_prestador)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql, new String[] {"id_paquete"})) {
            asignar(ps, p);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(Paquete p) throws SQLException {
        String sql = """
                UPDATE paquete
                   SET nombre = ?, descripcion = ?, duracion_dias = ?, categoria = ?, minimo_viajeros = ?,
                       visibilidad = ?, id_destino = ?, id_prestador = ?
                 WHERE id_paquete = ?
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, p);
            ps.setInt(9, p.id());
            ps.executeUpdate();
        }
    }

    /** Si el paquete tiene planes o salidas, Oracle lanza ORA-02292 (RESTRICT). */
    public void eliminar(int idPaquete) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM paquete WHERE id_paquete = ?")) {
            ps.setInt(1, idPaquete);
            ps.executeUpdate();
        }
    }

    /** Parámetros 1 a 8, en el mismo orden del INSERT y del UPDATE. */
    private void asignar(PreparedStatement ps, Paquete p) throws SQLException {
        ps.setString(1, p.nombre());
        ps.setString(2, p.descripcion());
        ps.setInt(3, p.duracionDias());
        ps.setString(4, p.categoria());
        ps.setInt(5, p.minimoViajeros());
        ps.setString(6, p.visibilidad());
        ps.setInt(7, p.idDestino());
        ps.setInt(8, p.idPrestador());
    }

    private Paquete mapear(ResultSet rs) throws SQLException {
        return new Paquete(
                rs.getInt("id_paquete"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getInt("duracion_dias"),
                rs.getString("categoria"),
                rs.getInt("minimo_viajeros"),
                rs.getString("visibilidad"),
                rs.getInt("id_destino"),
                rs.getString("destino"),
                rs.getInt("id_prestador"),
                rs.getString("prestador"));
    }
}
