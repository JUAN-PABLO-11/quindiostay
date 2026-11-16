package co.edu.uniquindio.quindiostay.vista;

import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;

import co.edu.uniquindio.quindiostay.logica.ClienteService;
import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.modelo.Cliente;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;
import co.edu.uniquindio.quindiostay.vista.comun.SelectorFecha;

/** Formulario para crear o editar un cliente. */
public class ClienteDialog extends JDialog{
    private final ClienteService servicio = new ClienteService();
    private final Cliente original;   // null = nuevo
    private boolean guardado = false;

    private final JTextField txtNombres = new JTextField(25);
    private final JTextField txtApellidos = new JTextField(25);
    private final JComboBox<String> cmbTipoDoc =
            new JComboBox<>(ClienteService.TIPOS_DOCUMENTO.toArray(String[]::new));
    private final JTextField txtDocumento = new JTextField(15);
    private final JTextField txtCorreo = new JTextField(30);
    private final JTextField txtTelefono = new JTextField(10);
    private final SelectorFecha selNacimiento = new SelectorFecha(1900, LocalDate.now().getYear());
    private final JTextField txtCiudad = new JTextField(25);
    private final JLabel lblEstado = new JLabel(" ");

    /** Abre el formulario y devuelve true si se guardó. */
    public static boolean mostrar(Component padre, Cliente cliente) {
        ClienteDialog dialogo = new ClienteDialog(SwingUtilities.getWindowAncestor(padre), cliente);
        dialogo.setVisible(true);   // modal: se queda aquí hasta que se cierre
        return dialogo.guardado;
    }

    private ClienteDialog(Window padre, Cliente cliente) {
        super(padre, cliente == null ? "Nuevo cliente" : "Editar cliente", ModalityType.APPLICATION_MODAL);
        this.original = cliente;

        Formulario f = new Formulario();
        f.campo("Nombres", txtNombres);
        f.campo("Apellidos", txtApellidos);
        f.campo("Tipo de documento", cmbTipoDoc);
        f.campo("Número de documento", txtDocumento);
        f.campo("Correo", txtCorreo);
        f.campo("Teléfono (celular)", txtTelefono);
        f.campo("Fecha de nacimiento", selNacimiento);
        f.campo("Ciudad de origen", txtCiudad);

        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnCancelar);
        botones.add(btnGuardar);

        lblEstado.setForeground(new Color(0xC62828));
        lblEstado.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        JPanel sur = new JPanel(new BorderLayout());
        sur.add(lblEstado, BorderLayout.CENTER);
        sur.add(botones, BorderLayout.SOUTH);

        add(f, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnGuardar);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        if (cliente != null) {
            txtNombres.setText(cliente.nombres());
            txtApellidos.setText(cliente.apellidos());
            cmbTipoDoc.setSelectedItem(cliente.tipoDocumento());
            txtDocumento.setText(cliente.numeroDocumento());
            txtCorreo.setText(cliente.correo());
            txtTelefono.setText(cliente.telefono());
            selNacimiento.setFecha(cliente.fechaNacimiento());
            txtCiudad.setText(cliente.ciudadOrigen());
        } else {
            selNacimiento.setFecha(LocalDate.of(1990, 1, 1));
        }
        pack();
        setLocationRelativeTo(padre);
    }

    private Cliente construir() {
        return new Cliente(
                original == null ? null : original.id(),
                txtNombres.getText().trim(),
                txtApellidos.getText().trim(),
                (String) cmbTipoDoc.getSelectedItem(),
                txtDocumento.getText().trim(),
                txtCorreo.getText().trim(),
                txtTelefono.getText().trim(),
                selNacimiento.getFecha(),
                txtCiudad.getText().trim());
    }

    private void guardar() {
        try {
            servicio.guardar(construir());
            guardado = true;
            dispose();
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
            pack();
        }
    }
}
