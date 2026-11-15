package co.edu.uniquindio.quindiostay.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.edu.uniquindio.quindiostay.modelo.PaqueteComponente;

/** Acceso a la tabla puente PAQUETE_COMPONENTE
 * y la relación N:M entre paquetes y componentes. */
public class PaqueteComponenteDAO {
    private static final String SELECT_BASE = """
            SELECT pc.id_paquete, pq.nombre AS paquete,
                   pc.id_componente, c.nombre AS componente, t.nombre AS tipo_componente
              FROM paquete_componente pc
              JOIN paquete pq        ON pq.id_paquete        = pc.id_paquete
              JOIN componente c      ON c.id_componente      = pc.id_componente
              JOIN tipo_componente t ON t.id_tipo_componente = c.id_tipo_componente
            """;

    /**
     * Lista las parejas paquete-componente cuyo paquete, componente o tipo contiene el filtro.
     *
     * @param idPrestador si no es null, solo las de paquetes de ese prestador
     */
    public List<PaqueteComponente> consultar(String filtro, Integer idPrestador) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE)
                .append(" WHERE (LOWER(pq.nombre) LIKE ? OR LOWER(c.nombre) LIKE ? OR LOWER(t.nombre) LIKE ?)");
        if (idPrestador != null) {
            sql.append(" AND pq.id_prestador = ?");
        }
        sql.append(" ORDER BY pq.nombre, t.nombre, c.nombre");
        String patron = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            if (idPrestador != null) {
                ps.setInt(4, idPrestador);
            }
            List<PaqueteComponente> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new PaqueteComponente(
                            rs.getInt("id_paquete"),
                            rs.getString("paquete"),
                            rs.getInt("id_componente"),
                            rs.getString("componente"),
                            rs.getString("tipo_componente")));
                }
            }
            return lista;
        }
    }

    /** Ids de los componentes que hoy tiene el paquete (para marcar las casillas del formulario). */
    public Set<Integer> idsDelPaquete(int idPaquete) throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            return idsDelPaquete(cn, idPaquete);
        }
    }

    /**
     * Deja el paquete con EXACTAMENTE estos componentes, en una sola transacción:
     * borra los que se desmarcaron e inserta los nuevos. O se guarda todo o nada.
     */
    public void reemplazar(int idPaquete, Set<Integer> nuevos) throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                Set<Integer> actuales = idsDelPaquete(cn, idPaquete);
                try (PreparedStatement borrar = cn.prepareStatement(
                             "DELETE FROM paquete_componente WHERE id_paquete = ? AND id_componente = ?");
                     PreparedStatement insertar = cn.prepareStatement(
                             "INSERT INTO paquete_componente (id_paquete, id_componente) VALUES (?, ?)")) {
                    for (Integer id : actuales) {
                        if (!nuevos.contains(id)) {
                            borrar.setInt(1, idPaquete);
                            borrar.setInt(2, id);
                            borrar.addBatch();
                        }
                    }
                    for (Integer id : nuevos) {
                        if (!actuales.contains(id)) {
                            insertar.setInt(1, idPaquete);
                            insertar.setInt(2, id);
                            insertar.addBatch();
                        }
                    }
                    borrar.executeBatch();
                    insertar.executeBatch();
                }
                cn.commit();
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    /** Quita un solo componente de un paquete. */
    public void eliminar(int idPaquete, int idComponente) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "DELETE FROM paquete_componente WHERE id_paquete = ? AND id_componente = ?")) {
            ps.setInt(1, idPaquete);
            ps.setInt(2, idComponente);
            ps.executeUpdate();
        }
    }

    private Set<Integer> idsDelPaquete(Connection cn, int idPaquete) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT id_componente FROM paquete_componente WHERE id_paquete = ?")) {
            ps.setInt(1, idPaquete);
            Set<Integer> ids = new HashSet<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            }
            return ids;
        }
    }
}
