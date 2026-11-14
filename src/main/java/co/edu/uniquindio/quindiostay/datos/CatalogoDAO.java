package co.edu.uniquindio.quindiostay.datos;

import co.edu.uniquindio.quindiostay.modelo.Opcion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Listas para los JComboBox de las llaves foráneas (id + texto).
 * Para un combo nuevo basta con agregar un método de una línea con su SELECT:
 * la primera columna es el id y la segunda el texto que se ve.
 */
public class CatalogoDAO {

    public List<Opcion> destinos() throws SQLException {
        return opciones("""
                SELECT d.id_destino, d.nombre || ' (' || m.nombre || ')'
                  FROM destino d JOIN municipio m ON m.id_municipio = d.id_municipio
                 ORDER BY d.nombre
                """);
    }

    /** RN02: solo los prestadores activos pueden crear paquetes o componentes. */
    public List<Opcion> prestadoresActivos() throws SQLException {
        return opciones("SELECT id_prestador, nombre_comercial FROM prestador WHERE estado = 'activo' ORDER BY nombre_comercial");
    }

    public List<Opcion> municipios() throws SQLException {
        return opciones("SELECT id_municipio, nombre FROM municipio ORDER BY nombre");
    }

    public List<Opcion> tiposPrestador() throws SQLException {
        return opciones("SELECT id_tipo_prestador, nombre FROM tipo_prestador ORDER BY nombre");
    }

    public List<Opcion> tiposComponente() throws SQLException {
        return opciones("SELECT id_tipo_componente, nombre FROM tipo_componente ORDER BY nombre");
    }

    public List<Opcion> roles() throws SQLException {
        return opciones("SELECT id_rol, nombre FROM rol ORDER BY nombre");
    }

    /** Todos los prestadores (activos o no): para ligar a un usuario encargado, que puede ser de uno suspendido. */
    public List<Opcion> prestadores() throws SQLException {
        return opciones("SELECT id_prestador, nombre_comercial FROM prestador ORDER BY nombre_comercial");
    }

    /** Ejecuta un SELECT de dos columnas (id, texto) y lo convierte en opciones. */
    private List<Opcion> opciones(String sql, Object... parametros) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            List<Opcion> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Opcion(rs.getInt(1), rs.getString(2)));
                }
            }
            return lista;
        }
    }
}
