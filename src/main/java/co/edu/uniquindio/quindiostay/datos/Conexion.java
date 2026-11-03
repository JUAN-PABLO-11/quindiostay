package co.edu.uniquindio.quindiostay.datos;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexiones JDBC a Oracle con los datos de db.properties (raíz del proyecto).
 * Uso en un DAO:
 *   try (Connection cn = Conexion.obtener();
 *        PreparedStatement ps = cn.prepareStatement("SELECT ...")) { ... }
 */
public final class Conexion {

    private static final String ARCHIVO = "db.properties";
    private static Properties config;

    private Conexion() { }

    private static synchronized Properties config() {
        if (config == null) {
            Properties p = new Properties();
            try (InputStream in = new FileInputStream(ARCHIVO)) {
                p.load(in);
            } catch (IOException e) {
                throw new IllegalStateException(
                    "No se encontró " + ARCHIVO + ". Copia db.properties.example como db.properties.", e);
            }
            config = p;
        }
        return config;
    }

    public static Connection obtener() throws SQLException {
        Properties p = config();
        return DriverManager.getConnection(
            p.getProperty("db.url"),
            p.getProperty("db.user"),
            p.getProperty("db.password"));
    }
}
