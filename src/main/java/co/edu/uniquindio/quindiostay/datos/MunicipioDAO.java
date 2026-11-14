package co.edu.uniquindio.quindiostay.datos;

import co.edu.uniquindio.quindiostay.modelo.Municipio;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla MUNICIPIO: consultar, insertar, actualizar y eliminar. */
public class MunicipioDAO {
    private static final String SELECT_BASE = "SELECT id_municipio, codigo_dane, nombre FROM municipio";

    /** Lista los municipios cuyo nombre o código DANE contiene el filtro (vacío = todos). */
    public List<Municipio> consultar(String filtro) throws SQLException {
        String patron = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     SELECT_BASE + " WHERE LOWER(nombre) LIKE ? OR codigo_dane LIKE ? ORDER BY nombre")) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            List<Municipio> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        }
    }

    /** Inserta y devuelve el id que generó Oracle (IDENTITY). */
    public int insertar(Municipio m) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "INSERT INTO municipio (codigo_dane, nombre) VALUES (?, ?)", new String[] {"id_municipio"})) {
            asignar(ps, m);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(Municipio m) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE municipio SET codigo_dane = ?, nombre = ? WHERE id_municipio = ?")) {
            asignar(ps, m);
            ps.setInt(3, m.id());
            ps.executeUpdate();
        }
    }

    /** Si tiene destinos o prestadores, Oracle lanza ORA-02292 (RESTRICT). */
    public void eliminar(int idMunicipio) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM municipio WHERE id_municipio = ?")) {
            ps.setInt(1, idMunicipio);
            ps.executeUpdate();
        }
    }

    /** Parámetros 1 y 2, en el mismo orden del INSERT y del UPDATE. */
    private void asignar(PreparedStatement ps, Municipio m) throws SQLException {
        ps.setString(1, m.codigoDane());
        ps.setString(2, m.nombre());
    }

    private Municipio mapear(ResultSet rs) throws SQLException {
        return new Municipio(rs.getInt("id_municipio"), rs.getString("codigo_dane"), rs.getString("nombre"));
    }   
}
