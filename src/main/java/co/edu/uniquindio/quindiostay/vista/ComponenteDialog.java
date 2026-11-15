package co.edu.uniquindio.quindiostay.vista;

import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
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
import co.edu.uniquindio.quindiostay.logica.ComponenteService;
import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.modelo.Componente;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;

/** Formulario para crear o editar un componente. */
public class ComponenteDialog extends JDialog{
    private final ComponenteService servicio = new ComponenteService();
    private final Componente original;   // null = nuevo
    private boolean guardado = false;

    private final JTextField txtNombre = new JTextField(30);
    private final JTextArea txtDescripcion;
    private final JSpinner spnCosto = new JSpinner(new SpinnerNumberModel(100000, 1, 2_000_000_000, 10000));
    private final JComboBox<Opcion> cmbTipo = new JComboBox<>();
    private final JComboBox<Opcion> cmbPrestador = new JComboBox<>();
    private final JLabel lblEstado = new JLabel(" ");

    /** Abre el formulario y devuelve true si se guardó. */
    public static boolean mostrar(Component padre, Componente componente) {
        ComponenteDialog dialogo = new ComponenteDialog(SwingUtilities.getWindowAncestor(padre), componente);
        dialogo.setVisible(true);   // modal: se queda aquí hasta que se cierre
        return dialogo.guardado;
    }

    private ComponenteDialog(Window padre, Componente componente) {
        super(padre, componente == null ? "Nuevo componente" : "Editar componente", ModalityType.APPLICATION_MODAL);
        this.original = componente;

        Formulario f = new Formulario();
        f.campo("Nombre", txtNombre);
        txtDescripcion = f.areaTexto("Descripción (opcional)", 3);
        f.campo("Costo ($)", spnCosto);
        f.campo("Tipo de componente", cmbTipo);
        f.campo("Prestador", cmbPrestador);

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
        if (componente != null) {
            llenar(componente);
        } else {
            cmbTipo.setSelectedIndex(-1);
        }
        // El encargado de prestador solo crea componentes de su propio prestador
        if (Sesion.esEncargado()) {
            Formulario.seleccionar(cmbPrestador, Sesion.actual().idPrestador(), "");
            cmbPrestador.setEnabled(false);
        }
        pack();
        setLocationRelativeTo(padre);
    }

    private void cargarListas() {
        try {
            CatalogoDAO catalogo = new CatalogoDAO();
            Formulario.llenar(cmbTipo, catalogo.tiposComponente());
            Formulario.llenar(cmbPrestador, catalogo.prestadoresActivos());   // RN02
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        }
    }

    private void llenar(Componente c) {
        txtNombre.setText(c.nombre());
        txtDescripcion.setText(c.descripcion() == null ? "" : c.descripcion());
        spnCosto.setValue(c.costo());
        Formulario.seleccionar(cmbTipo, c.idTipoComponente(), c.tipoComponente());
        Formulario.seleccionar(cmbPrestador, c.idPrestador(), c.prestador());
    }

    private Componente construir() {
        String descripcion = txtDescripcion.getText().trim();
        return new Componente(
                original == null ? null : original.id(),
                txtNombre.getText().trim(),
                descripcion.isEmpty() ? null : descripcion,
                (Integer) spnCosto.getValue(),
                Formulario.idSeleccionado(cmbTipo),
                Formulario.textoSeleccionado(cmbTipo),
                Formulario.idSeleccionado(cmbPrestador),
                Formulario.textoSeleccionado(cmbPrestador));
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
