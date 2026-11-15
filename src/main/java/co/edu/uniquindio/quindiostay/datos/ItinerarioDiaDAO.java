package co.edu.uniquindio.quindiostay.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.quindiostay.modelo.ItinerarioDia;

/** Acceso a la tabla ITINERARIO_DIA 
 * PK compuesta: id_paquete + numero_dia. */
public class ItinerarioDiaDAO {
    private static final String SELECT_BASE = """
            SELECT i.id_paquete, pq.nombre AS paquete, i.numero_dia, i.descripcion
              FROM itinerario_dia i
              JOIN paquete pq ON pq.id_paquete = i.id_paquete
            """;

    /**
     * Lista los días cuyo paquete o descripción contiene el filtro.
     * @param idPrestador si no es null, solo los de paquetes de ese prestador
     */
    public List<ItinerarioDia> consultar(String filtro, Integer idPrestador) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE)
                .append(" WHERE (LOWER(pq.nombre) LIKE ? OR LOWER(i.descripcion) LIKE ?)");
        if (idPrestador != null) {
            sql.append(" AND pq.id_prestador = ?");
        }
        sql.append(" ORDER BY pq.nombre, i.numero_dia");
        String patron = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            if (idPrestador != null) {
                ps.setInt(3, idPrestador);
            }
            List<ItinerarioDia> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ItinerarioDia(
                            rs.getInt("id_paquete"),
                            rs.getString("paquete"),
                            rs.getInt("numero_dia"),
                            rs.getString("descripcion")));
                }
            }
            return lista;
        }
    }

    /** El trigger RN11 revisa que el día no supere la duración del paquete. */
    public void insertar(ItinerarioDia d) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "INSERT INTO itinerario_dia (id_paquete, numero_dia, descripcion) VALUES (?, ?, ?)")) {
            ps.setInt(1, d.idPaquete());
            ps.setInt(2, d.numeroDia());
            ps.setString(3, d.descripcion());
            ps.executeUpdate();
        }
    }

    /** Solo cambia la descripción. */
    public void actualizar(ItinerarioDia d) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE itinerario_dia SET descripcion = ? WHERE id_paquete = ? AND numero_dia = ?")) {
            ps.setString(1, d.descripcion());
            ps.setInt(2, d.idPaquete());
            ps.setInt(3, d.numeroDia());
            ps.executeUpdate();
        }
    }

    public void eliminar(int idPaquete, int numeroDia) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "DELETE FROM itinerario_dia WHERE id_paquete = ? AND numero_dia = ?")) {
            ps.setInt(1, idPaquete);
            ps.setInt(2, numeroDia);
            ps.executeUpdate();
        }
    }
}
