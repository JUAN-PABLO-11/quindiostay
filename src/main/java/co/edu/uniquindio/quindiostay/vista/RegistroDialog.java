package co.edu.uniquindio.quindiostay.vista;

import co.edu.uniquindio.quindiostay.logica.CuentaService;
import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.modelo.RegistroCliente;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

/**
 * "Crear cuenta" del cliente, desde el enlace de la pantalla de inicio de sesión.
 */
public class RegistroDialog extends JDialog {

    private final CuentaService servicio = new CuentaService();
    private String correoRegistrado;

    private final JTextField txtNombres = new JTextField(26);
    private final JTextField txtApellidos = new JTextField(26);
    private final JComboBox<String> cmbTipoDoc =
            new JComboBox<>(CuentaService.TIPOS_DOCUMENTO.toArray(String[]::new));
    private final JTextField txtDocumento = new JTextField(26);
    private final JTextField txtCorreo = new JTextField(26);
    private final JTextField txtTelefono = new JTextField(26);
    private final JTextField txtNacimiento = new JTextField(26);
    private final JTextField txtCiudad = new JTextField(26);
    private final JPasswordField txtClave = new JPasswordField(26);
    private final JPasswordField txtConfirmacion = new JPasswordField(26);
    private final JComboBox<String> cmbIdioma = new JComboBox<>(new String[] {"español", "inglés"});
    private final JComboBox<String> cmbCanal = new JComboBox<>(new String[] {"correo", "sms", "whatsapp"});
    private final JComboBox<Integer> cmbDias = new JComboBox<>(new Integer[] {1, 3, 7});
    private final JCheckBox chkPromociones = new JCheckBox("Quiero recibir promociones");
    private final JCheckBox chkDatos = new JCheckBox("Autorizo el tratamiento de mis datos personales (Ley 1581 de 2012)");
    private final JLabel lblEstado = new JLabel(" ");

    /** Abre el formulario; devuelve el correo si se creó la cuenta, o null si se canceló. */
    public static String mostrar(Component padre) {
        RegistroDialog dialogo = new RegistroDialog(SwingUtilities.getWindowAncestor(padre));
        dialogo.setVisible(true);
        return dialogo.correoRegistrado;
    }

    private RegistroDialog(Window padre) {
        super(padre, "Crear cuenta", ModalityType.APPLICATION_MODAL);

        txtTelefono.putClientProperty("JTextField.placeholderText", "3001234567");
        txtNacimiento.putClientProperty("JTextField.placeholderText", "aaaa-mm-dd");
        txtClave.putClientProperty("JTextField.placeholderText", "Mínimo 8, con letras y números");
        cmbDias.setSelectedItem(3);

        Formulario f = new Formulario();
        f.campo("Nombres", txtNombres);
        f.campo("Apellidos", txtApellidos);
        f.campo("Tipo de documento", cmbTipoDoc);
        f.campo("Número de documento", txtDocumento);
        f.campo("Correo", txtCorreo);
        f.campo("Celular", txtTelefono);
        f.campo("Fecha de nacimiento", txtNacimiento);
        f.campo("Ciudad de origen", txtCiudad);
        f.campo("Contraseña", txtClave);
        f.campo("Confirmar contraseña", txtConfirmacion);
        f.campo(" ", new JSeparator());
        f.campo("Idioma", cmbIdioma);
        f.campo("Avisos por", cmbCanal);
        f.campo("Recordar con (días)", cmbDias);
        f.campo(" ", chkPromociones);
        f.campo(" ", chkDatos);

        JButton btnCrear = new JButton("Crear cuenta");
        JButton btnCancelar = new JButton("Cancelar");
        btnCrear.addActionListener(e -> crear());
        btnCancelar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnCancelar);
        botones.add(btnCrear);

        lblEstado.setForeground(new Color(0xC62828));
        lblEstado.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        JPanel sur = new JPanel(new BorderLayout());
        sur.add(lblEstado, BorderLayout.CENTER);
        sur.add(botones, BorderLayout.SOUTH);

        add(f, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnCrear);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        pack();
        setLocationRelativeTo(padre);
    }

    private void crear() {
        char[] clave = txtClave.getPassword();
        char[] confirmacion = txtConfirmacion.getPassword();
        try {
            if (!chkDatos.isSelected()) {
                lblEstado.setText("Para crear la cuenta debes autorizar el tratamiento de tus datos personales.");
                return;
            }
            LocalDate nacimiento;
            try {
                nacimiento = LocalDate.parse(txtNacimiento.getText().trim());
            } catch (DateTimeParseException ex) {
                lblEstado.setText("Escribe la fecha de nacimiento como aaaa-mm-dd, por ejemplo 1998-04-25.");
                return;
            }
            RegistroCliente r = new RegistroCliente(
                    txtNombres.getText().trim(),
                    txtApellidos.getText().trim(),
                    (String) cmbTipoDoc.getSelectedItem(),
                    txtDocumento.getText().trim(),
                    txtCorreo.getText().trim().toLowerCase(),
                    txtTelefono.getText().trim(),
                    nacimiento,
                    txtCiudad.getText().trim(),
                    cmbIdioma.getSelectedIndex() == 0 ? "espanol" : "ingles",
                    (String) cmbCanal.getSelectedItem(),
                    (Integer) cmbDias.getSelectedItem(),
                    chkPromociones.isSelected());

            servicio.registrar(r, clave, confirmacion);
            correoRegistrado = r.correo();
            JOptionPane.showMessageDialog(this, "Cuenta creada. Ya puedes iniciar sesión con tu correo.",
                    "Crear cuenta", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        } finally {
            Arrays.fill(clave, ' ');
            Arrays.fill(confirmacion, ' ');
        }
    }
}
