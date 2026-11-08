package co.edu.uniquindio.quindiostay.vista;

import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.logica.LoginService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.util.Arrays;

/**
 * Inicio de sesión único: usuarios internos (con su nombre de usuario)
 * y clientes (con su correo).
 */
public class LoginFrame extends JFrame {

    private final JTextField txtUsuario = new JTextField(22);
    private final JPasswordField txtClave = new JPasswordField(22);
    private final JLabel lblEstado = new JLabel(" ", SwingConstants.CENTER);

    public LoginFrame() {
        super("QuindioStay · Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel lblTitulo = new JLabel("QuindioStay", SwingConstants.CENTER);
        lblTitulo.putClientProperty("FlatLaf.styleClass", "h1");
        JLabel lblSubtitulo = new JLabel("Paquetes turísticos del Quindío", SwingConstants.CENTER);

        txtUsuario.putClientProperty("JTextField.placeholderText", "Usuario o correo");
        txtClave.putClientProperty("JTextField.placeholderText", "Contraseña");

        JButton btnIngresar = new JButton("Ingresar");
        btnIngresar.addActionListener(e -> ingresar());

        JButton btnCrearCuenta = new JButton("¿Eres cliente y no tienes cuenta? Créala aquí");
        btnCrearCuenta.setBorderPainted(false);
        btnCrearCuenta.setContentAreaFilled(false);
        btnCrearCuenta.setForeground(new Color(0x1565C0));
        btnCrearCuenta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCrearCuenta.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "El registro de clientes aún está en construcción.", "Crear cuenta",
                JOptionPane.INFORMATION_MESSAGE));

        lblEstado.setForeground(new Color(0xC62828));

        JPanel campos = new JPanel(new GridLayout(0, 1, 0, 8));
        campos.add(txtUsuario);
        campos.add(txtClave);
        campos.add(btnIngresar);
        campos.add(lblEstado);
        campos.add(btnCrearCuenta);

        JPanel encabezado = new JPanel(new GridLayout(0, 1, 0, 4));
        encabezado.add(lblTitulo);
        encabezado.add(lblSubtitulo);

        JPanel raiz = new JPanel(new BorderLayout(0, 20));
        raiz.setBorder(BorderFactory.createEmptyBorder(30, 40, 24, 40));
        raiz.add(encabezado, BorderLayout.NORTH);
        raiz.add(campos, BorderLayout.CENTER);

        setContentPane(raiz);
        getRootPane().setDefaultButton(btnIngresar);   // Enter = Ingresar
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void ingresar() {
        char[] clave = txtClave.getPassword();
        try {
            new LoginService().iniciarSesion(txtUsuario.getText(), clave);
            dispose();
            new VentanaPrincipal().setVisible(true);
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
            txtClave.setText("");
            txtClave.requestFocusInWindow();
        } finally {
            Arrays.fill(clave, ' ');   // no dejar la contraseña en memoria
        }
    }
}
