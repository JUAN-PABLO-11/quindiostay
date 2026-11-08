package co.edu.uniquindio.quindiostay;

import co.edu.uniquindio.quindiostay.vista.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.SwingUtilities;

/** Punto de entrada: aplica el aspecto FlatLaf y abre el inicio de sesión. */
public class Main {

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
