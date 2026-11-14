package co.edu.uniquindio.quindiostay.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.quindiostay.modelo.Prestador;

/** Acceso a la tabla PRESTADOR: consultar, insertar, actualizar y eliminar. */
public class PrestadorDAO {
    private static final String SELECT_BASE = """
            SELECT p.id_prestador, p.nombre_comercial, p.nit, p.numero_rnt, p.direccion,
                   p.telefono, p.correo, p.estado,
                   p.id_tipo_prestador, t.nombre AS tipo_prestador,
                   p.id_municipio, m.nombre AS municipio
              FROM prestador p
              JOIN tipo_prestador t ON t.id_tipo_prestador = p.id_tipo_prestador
              JOIN municipio m      ON m.id_municipio      = p.id_municipio
            """;

    /** Lista los prestadores cuyo nombre, NIT, tipo, municipio o estado contiene el filtro (vacío = todos). */
    public List<Prestador> consultar(String filtro) throws SQLException {
        String sql = SELECT_BASE + """
                 WHERE LOWER(p.nombre_comercial) LIKE ? OR p.nit LIKE ? OR LOWER(t.nombre) LIKE ?
                    OR LOWER(m.nombre) LIKE ? OR LOWER(p.estado) LIKE ?
                 ORDER BY p.nombre_comercial
                """;
        String patron = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, patron);
            }
            List<Prestador> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        }
    }

    /** Inserta y devuelve el id que generó Oracle (IDENTITY). */
    public int insertar(Prestador p) throws SQLException {
        String sql = """
                INSERT INTO prestador (nombre_comercial, nit, numero_rnt, direccion, telefono, correo,
                                       estado, id_tipo_prestador, id_municipio)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql, new String[] {"id_prestador"})) {
            asignar(ps, p);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(Prestador p) throws SQLException {
        String sql = """
                UPDATE prestador
                   SET nombre_comercial = ?, nit = ?, numero_rnt = ?, direccion = ?, telefono = ?,
                       correo = ?, estado = ?, id_tipo_prestador = ?, id_municipio = ?
                 WHERE id_prestador = ?
                """;
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, p);
            ps.setInt(10, p.id());
            ps.executeUpdate();
        }
    }

    /** Si tiene paquetes, componentes o cotizaciones, Oracle lanza ORA-02292 (RESTRICT). */
    public void eliminar(int idPrestador) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM prestador WHERE id_prestador = ?")) {
            ps.setInt(1, idPrestador);
            ps.executeUpdate();
        }
    }

    /** Parámetros 1 a 9, en el mismo orden del INSERT y del UPDATE. */
    private void asignar(PreparedStatement ps, Prestador p) throws SQLException {
        ps.setString(1, p.nombreComercial());
        ps.setString(2, p.nit());
        ps.setString(3, p.numeroRnt());
        ps.setString(4, p.direccion());
        ps.setString(5, p.telefono());
        ps.setString(6, p.correo());
        ps.setString(7, p.estado());
        ps.setInt(8, p.idTipoPrestador());
        ps.setInt(9, p.idMunicipio());
    }

    private Prestador mapear(ResultSet rs) throws SQLException {
        return new Prestador(
                rs.getInt("id_prestador"),
                rs.getString("nombre_comercial"),
                rs.getString("nit"),
                rs.getString("numero_rnt"),
                rs.getString("direccion"),
                rs.getString("telefono"),
                rs.getString("correo"),
                rs.getString("estado"),
                rs.getInt("id_tipo_prestador"),
                rs.getString("tipo_prestador"),
                rs.getInt("id_municipio"),
                rs.getString("municipio"));
    }
}
