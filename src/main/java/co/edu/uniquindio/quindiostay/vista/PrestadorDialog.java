package co.edu.uniquindio.quindiostay.vista;

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

import co.edu.uniquindio.quindiostay.datos.CatalogoDAO;
import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.logica.PrestadorService;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.modelo.Prestador;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;

/** Formulario para crear o editar un prestador. */
public class PrestadorDialog extends JDialog {
    private final PrestadorService servicio = new PrestadorService();
    private final Prestador original;   // null = nuevo
    private boolean guardado = false;

    private final JTextField txtNombre = new JTextField(30);
    private final JTextField txtNit = new JTextField(15);
    private final JTextField txtRnt = new JTextField(10);
    private final JTextField txtDireccion = new JTextField(30);
    private final JTextField txtTelefono = new JTextField(10);
    private final JTextField txtCorreo = new JTextField(30);
    private final JComboBox<String> cmbEstado =
            new JComboBox<>(PrestadorService.ESTADOS.toArray(String[]::new));
    private final JComboBox<Opcion> cmbTipo = new JComboBox<>();
    private final JComboBox<Opcion> cmbMunicipio = new JComboBox<>();
    private final JLabel lblEstado = new JLabel(" ");

    /** Abre el formulario y devuelve true si se guardó. */
    public static boolean mostrar(Component padre, Prestador prestador) {
        PrestadorDialog dialogo = new PrestadorDialog(SwingUtilities.getWindowAncestor(padre), prestador);
        dialogo.setVisible(true);   // modal: se queda aquí hasta que se cierre
        return dialogo.guardado;
    }

    private PrestadorDialog(Window padre, Prestador prestador) {
        super(padre, prestador == null ? "Nuevo prestador" : "Editar prestador", ModalityType.APPLICATION_MODAL);
        this.original = prestador;

        Formulario f = new Formulario();
        f.campo("Nombre comercial", txtNombre);
        f.campo("NIT", txtNit);
        f.campo("Número RNT", txtRnt);
        f.campo("Dirección", txtDireccion);
        f.campo("Teléfono", txtTelefono);
        f.campo("Correo", txtCorreo);
        f.campo("Tipo de prestador", cmbTipo);
        f.campo("Municipio", cmbMunicipio);
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
        if (prestador != null) {
            llenar(prestador);
        }
        pack();
        setLocationRelativeTo(padre);
    }

    private void cargarListas() {
        try {
            CatalogoDAO catalogo = new CatalogoDAO();
            Formulario.llenar(cmbTipo, catalogo.tiposPrestador());
            Formulario.llenar(cmbMunicipio, catalogo.municipios());
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        }
    }

    /** Pone los datos del prestador en los campos (modo editar). */
    private void llenar(Prestador p) {
        txtNombre.setText(p.nombreComercial());
        txtNit.setText(p.nit());
        txtRnt.setText(p.numeroRnt());
        txtDireccion.setText(p.direccion());
        txtTelefono.setText(p.telefono());
        txtCorreo.setText(p.correo());
        cmbEstado.setSelectedItem(p.estado());
        Formulario.seleccionar(cmbTipo, p.idTipoPrestador(), p.tipoPrestador());
        Formulario.seleccionar(cmbMunicipio, p.idMunicipio(), p.municipio());
    }

    /** Arma el objeto con lo que hay en los campos. */
    private Prestador construir() {
        return new Prestador(
                original == null ? null : original.id(),
                txtNombre.getText().trim(),
                txtNit.getText().trim(),
                txtRnt.getText().trim(),
                txtDireccion.getText().trim(),
                txtTelefono.getText().trim(),
                txtCorreo.getText().trim(),
                Formulario.textoSeleccionado(cmbEstado),
                Formulario.idSeleccionado(cmbTipo),
                Formulario.textoSeleccionado(cmbTipo),
                Formulario.idSeleccionado(cmbMunicipio),
                Formulario.textoSeleccionado(cmbMunicipio));
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
