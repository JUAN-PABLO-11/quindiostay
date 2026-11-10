package co.edu.uniquindio.quindiostay.vista;

import co.edu.uniquindio.quindiostay.logica.CuentaService;
import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.modelo.PerfilCliente;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.Map;

/**
 * "Mi perfil": el cliente actualiza su contacto y sus preferencias de notificación,
 * cambia su contraseña y descarga su autorización de tratamiento de datos.
 */
public class MiPerfilPanel extends JPanel {

    private final CuentaService servicio = new CuentaService();
    private PerfilCliente perfil;

    private final JLabel lblNombre = new JLabel();
    private final JLabel lblDocumento = new JLabel();
    private final JLabel lblCorreo = new JLabel();
    private final JTextField txtTelefono = new JTextField(20);
    private final JTextField txtCiudad = new JTextField(20);
    private final JComboBox<String> cmbIdioma = new JComboBox<>(new String[] {"español", "inglés"});
    private final JComboBox<String> cmbCanal = new JComboBox<>(new String[] {"correo", "sms", "whatsapp"});
    private final JComboBox<Integer> cmbDias = new JComboBox<>(new Integer[] {1, 3, 7});
    private final JCheckBox chkPromos = new JCheckBox("Quiero recibir promociones");
    private final JPasswordField txtActual = new JPasswordField(20);
    private final JPasswordField txtNueva = new JPasswordField(20);
    private final JPasswordField txtConfirmar = new JPasswordField(20);
    private final JLabel lblEstado = new JLabel(" ");

    public MiPerfilPanel() {
        super(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));
        JLabel titulo = new JLabel("Mi perfil");
        titulo.putClientProperty("FlatLaf.styleClass", "h2");

        Formulario f = new Formulario();
        f.campo("Nombre", lblNombre);
        f.campo("Documento", lblDocumento);
        f.campo("Correo (usuario)", lblCorreo);
        f.campo("Celular", txtTelefono);
        f.campo("Ciudad de origen", txtCiudad);
        f.campo(" ", new JSeparator());
        f.campo("Idioma", cmbIdioma);
        f.campo("Avisos por", cmbCanal);
        f.campo("Recordar con (días)", cmbDias);
        f.campo(" ", chkPromos);
        JButton btnGuardar = new JButton("Guardar cambios");
        btnGuardar.addActionListener(e -> guardar());
        f.campo(" ", fila(btnGuardar));
        f.campo(" ", new JSeparator());
        f.campo("Contraseña actual", txtActual);
        f.campo("Contraseña nueva", txtNueva);
        f.campo("Confirmar nueva", txtConfirmar);
        JButton btnClave = new JButton("Cambiar contraseña");
        btnClave.addActionListener(e -> cambiarClave());
        f.campo(" ", fila(btnClave));
        f.campo(" ", new JSeparator());
        JButton btnDatos = new JButton("Ver mi autorización de tratamiento de datos (PDF)");
        btnDatos.addActionListener(e -> VisorReporte.abrir(this, "autorizacion_datos",
                Map.of("ID_CLIENTE", perfil.idCliente()), "Autorización de datos " + perfil.idCliente()));
        f.campo("Mis datos", fila(btnDatos));

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(f, BorderLayout.NORTH);
        add(titulo, BorderLayout.NORTH);
        add(new javax.swing.JScrollPane(centro), BorderLayout.CENTER);
        add(lblEstado, BorderLayout.SOUTH);
        cargar();
    }

    private static JPanel fila(JButton boton) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.add(boton);
        return p;
    }

    private void cargar() {
        try {
            perfil = servicio.miPerfil();
            lblNombre.setText(perfil.nombre());
            lblDocumento.setText(perfil.documento());
            lblCorreo.setText(perfil.correo());
            txtTelefono.setText(perfil.telefono());
            txtCiudad.setText(perfil.ciudadOrigen());
            cmbIdioma.setSelectedIndex("ingles".equals(perfil.idioma()) ? 1 : 0);
            cmbCanal.setSelectedItem(perfil.canalPreferido());
            cmbDias.setSelectedItem(perfil.diasAnticipacion());
            chkPromos.setSelected(perfil.aceptaPromociones());
        } catch (Exception e) {
            error(e);
        }
    }

    private void guardar() {
        PerfilCliente nuevo = new PerfilCliente(perfil.idCliente(), perfil.nombre(), perfil.documento(), perfil.correo(),
                txtTelefono.getText().trim(), txtCiudad.getText().trim(),
                cmbIdioma.getSelectedIndex() == 0 ? "espanol" : "ingles",
                (String) cmbCanal.getSelectedItem(), (Integer) cmbDias.getSelectedItem(), chkPromos.isSelected());
        try {
            servicio.guardarPerfil(nuevo);
            perfil = nuevo;
            info("Cambios guardados. Tus próximos recordatorios usarán estas preferencias.");
        } catch (Exception e) {
            error(e);
        }
    }

    private void cambiarClave() {
        char[] a = txtActual.getPassword();
        char[] n = txtNueva.getPassword();
        char[] c = txtConfirmar.getPassword();
        try {
            servicio.cambiarClave(a, n, c);
            txtActual.setText("");
            txtNueva.setText("");
            txtConfirmar.setText("");
            info("Contraseña cambiada.");
        } catch (Exception e) {
            error(e);
        } finally {
            Arrays.fill(a, ' ');
            Arrays.fill(n, ' ');
            Arrays.fill(c, ' ');
        }
    }

    private void info(String m) {
        lblEstado.setForeground(new Color(0x2E5E3A));
        lblEstado.setText(m);
    }

    private void error(Exception e) {
        lblEstado.setForeground(new Color(0xC62828));
        lblEstado.setText(ErroresBD.mensaje(e));
    }
}
