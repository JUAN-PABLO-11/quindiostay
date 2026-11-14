package co.edu.uniquindio.quindiostay.vista;

import javax.swing.JPasswordField;
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
import java.util.Arrays;

import co.edu.uniquindio.quindiostay.datos.CatalogoDAO;
import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.logica.UsuarioSistemaService;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.modelo.UsuarioSistema;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;

/**
 * Formulario para crear o editar un usuario interno.
 * Al editar, la contraseña vacía significa "no cambiarla". 
 * El combo de prestador solo se habilita con el rol "encargado de prestador" (RN03).
 */
public class UsuarioSistemaDialog extends JDialog{
    private final UsuarioSistemaService servicio = new UsuarioSistemaService();
    private final UsuarioSistema original;   // null = nuevo
    private boolean guardado = false;

    private final JTextField txtUsuario = new JTextField(20);
    private final JPasswordField txtClave = new JPasswordField(20);
    private final JComboBox<String> cmbEstado =
            new JComboBox<>(UsuarioSistemaService.ESTADOS.toArray(String[]::new));
    private final JComboBox<Opcion> cmbRol = new JComboBox<>();
    private final JComboBox<Opcion> cmbPrestador = new JComboBox<>();
    private final JLabel lblEstado = new JLabel(" ");

    /** Abre el formulario y devuelve true si se guardó. */
    public static boolean mostrar(Component padre, UsuarioSistema usuario) {
        UsuarioSistemaDialog dialogo = new UsuarioSistemaDialog(SwingUtilities.getWindowAncestor(padre), usuario);
        dialogo.setVisible(true);   // modal: se queda aquí hasta que se cierre
        return dialogo.guardado;
    }

    private UsuarioSistemaDialog(Window padre, UsuarioSistema usuario) {
        super(padre, usuario == null ? "Nuevo usuario del sistema" : "Editar usuario del sistema",
                ModalityType.APPLICATION_MODAL);
        this.original = usuario;

        txtClave.putClientProperty("JTextField.placeholderText",
                usuario == null ? "Mínimo 8 caracteres" : "Vacía = no cambiarla");

        Formulario f = new Formulario();
        f.campo("Usuario", txtUsuario);
        f.campo("Contraseña", txtClave);
        f.campo("Rol", cmbRol);
        f.campo("Prestador (solo encargado)", cmbPrestador);
        f.campo("Estado", cmbEstado);

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

        cargarListas();
        cmbRol.addActionListener(e -> aplicarRol());
        if (usuario != null) {
            llenar(usuario);
        }
        aplicarRol();

        pack();
        setLocationRelativeTo(padre);
    }

    private void cargarListas() {
        try {
            CatalogoDAO catalogo = new CatalogoDAO();
            Formulario.llenar(cmbRol, catalogo.roles());
            Formulario.llenar(cmbPrestador, catalogo.prestadores());
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        }
    }

    /** RN03: el prestador solo aplica al rol "encargado de prestador". */
    private void aplicarRol() {
        boolean encargado = Sesion.ENCARGADO.equals(Formulario.textoSeleccionado(cmbRol));
        cmbPrestador.setEnabled(encargado);
        if (!encargado) {
            cmbPrestador.setSelectedIndex(-1);
        }
    }

    /** Pone los datos del usuario en los campos (modo editar). La contraseña no se muestra. */
    private void llenar(UsuarioSistema u) {
        txtUsuario.setText(u.nombreUsuario());
        cmbEstado.setSelectedItem(u.estado());
        Formulario.seleccionar(cmbRol, u.idRol(), u.rol());
        if (u.idPrestador() != null) {
            Formulario.seleccionar(cmbPrestador, u.idPrestador(), u.prestador());
        }
    }

    /** Arma el objeto con lo que hay en los campos. */
    private UsuarioSistema construir() {
        int idPrestador = Formulario.idSeleccionado(cmbPrestador);
        boolean conPrestador = cmbPrestador.isEnabled() && idPrestador > 0;
        return new UsuarioSistema(
                original == null ? null : original.id(),
                txtUsuario.getText().trim(),
                Formulario.textoSeleccionado(cmbEstado),
                Formulario.idSeleccionado(cmbRol),
                Formulario.textoSeleccionado(cmbRol),
                conPrestador ? idPrestador : null,
                conPrestador ? Formulario.textoSeleccionado(cmbPrestador) : null);
    }

    private void guardar() {
        char[] clave = txtClave.getPassword();
        try {
            servicio.guardar(construir(), clave);
            guardado = true;
            dispose();
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
            pack();
        } finally {
            Arrays.fill(clave, '\0');   // la contraseña no debe quedarse en memoria
        }
    }
}
