package co.edu.uniquindio.quindiostay;

import co.edu.uniquindio.quindiostay.datos.Conexion;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Punto de entrada. Por ahora solo prueba la conexión con Oracle;
 * cuando exista la ventana de login, se abre desde aquí.
 */
public class Main {

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(Main::probarConexion);
    }

    private static void probarConexion() {
        try (Connection cn = Conexion.obtener();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery("SELECT banner FROM v$version WHERE ROWNUM = 1")) {
            rs.next();
            JOptionPane.showMessageDialog(null,
                "Conexión exitosa.\n" + rs.getString(1),
                "QuindioStay", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "No se pudo conectar a Oracle:\n" + e.getMessage(),
                "QuindioStay", JOptionPane.ERROR_MESSAGE);
        }
    }
}
