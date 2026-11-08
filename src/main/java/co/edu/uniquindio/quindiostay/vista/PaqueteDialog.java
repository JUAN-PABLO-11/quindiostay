package co.edu.uniquindio.quindiostay.vista;

import co.edu.uniquindio.quindiostay.datos.CatalogoDAO;
import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.logica.PaqueteService;
import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.modelo.Paquete;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;

/**
 * Formulario para crear o editar un paquete.
 *
 * PLANTILLA: para otra tabla, copiar este archivo y cambiar los campos,
 * llenar() y construir(). La forma de abrirlo desde el panel es:
 *   if (PaqueteDialog.mostrar(this, null)) { cargar(); }       // nuevo
 *   if (PaqueteDialog.mostrar(this, paquete)) { cargar(); }    // editar
 */
public class PaqueteDialog extends JDialog {

    private final PaqueteService servicio = new PaqueteService();
    private final Paquete original;   // null = paquete nuevo
    private boolean guardado = false;

    private final JTextField txtNombre = new JTextField(30);
    private final JTextArea txtDescripcion;
    private final JSpinner spnDuracion = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));
    private final JComboBox<String> cmbCategoria =
            new JComboBox<>(PaqueteService.CATEGORIAS.toArray(String[]::new));
    private final JSpinner spnMinimo = new JSpinner(new SpinnerNumberModel(2, 1, 50, 1));
    private final JComboBox<String> cmbVisibilidad =
            new JComboBox<>(PaqueteService.VISIBILIDADES.toArray(String[]::new));
    private final JComboBox<Opcion> cmbDestino = new JComboBox<>();
    private final JComboBox<Opcion> cmbPrestador = new JComboBox<>();
    private final JLabel lblEstado = new JLabel(" ");

    /** Abre el formulario y devuelve true si se guardó. */
    public static boolean mostrar(Component padre, Paquete paquete) {
        PaqueteDialog dialogo = new PaqueteDialog(SwingUtilities.getWindowAncestor(padre), paquete);
        dialogo.setVisible(true);   // modal: se queda aquí hasta que se cierre
        return dialogo.guardado;
    }

    private PaqueteDialog(Window padre, Paquete paquete) {
        super(padre, paquete == null ? "Nuevo paquete" : "Editar paquete", ModalityType.APPLICATION_MODAL);
        this.original = paquete;

        Formulario f = new Formulario();
        f.campo("Nombre", txtNombre);
        txtDescripcion = f.areaTexto("Descripción", 4);
        f.campo("Duración (días)", spnDuracion);
        f.campo("Categoría", cmbCategoria);
        f.campo("Mínimo de viajeros", spnMinimo);
        f.campo("Visibilidad", cmbVisibilidad);
        f.campo("Destino", cmbDestino);
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
        if (paquete != null) {
            llenar(paquete);
        } else {
            cmbCategoria.setSelectedIndex(-1);
        }
        // El encargado de prestador solo crea paquetes de su propio prestador
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
            Formulario.llenar(cmbDestino, catalogo.destinos());
            Formulario.llenar(cmbPrestador, catalogo.prestadoresActivos());
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        }
    }

    /** Pone los datos del paquete en los campos (modo editar). */
    private void llenar(Paquete p) {
        txtNombre.setText(p.nombre());
        txtDescripcion.setText(p.descripcion());
        spnDuracion.setValue(p.duracionDias());
        cmbCategoria.setSelectedItem(p.categoria());
        spnMinimo.setValue(p.minimoViajeros());
        cmbVisibilidad.setSelectedItem(p.visibilidad());
        Formulario.seleccionar(cmbDestino, p.idDestino(), p.destino());
        Formulario.seleccionar(cmbPrestador, p.idPrestador(), p.prestador());
    }

    /** Arma el objeto con lo que hay en los campos. */
    private Paquete construir() {
        return new Paquete(
                original == null ? null : original.id(),
                txtNombre.getText().trim(),
                txtDescripcion.getText().trim(),
                (Integer) spnDuracion.getValue(),
                Formulario.textoSeleccionado(cmbCategoria),
                (Integer) spnMinimo.getValue(),
                Formulario.textoSeleccionado(cmbVisibilidad),
                Formulario.idSeleccionado(cmbDestino),
                Formulario.textoSeleccionado(cmbDestino),
                Formulario.idSeleccionado(cmbPrestador),
                Formulario.textoSeleccionado(cmbPrestador));
    }

    private void guardar() {
        try {
            servicio.guardar(construir());
            guardado = true;
            dispose();
        } catch (Exception e) {
            // Validación de la lógica o error de Oracle: se muestra y el formulario sigue abierto
            lblEstado.setText(ErroresBD.mensaje(e));
            pack();
        }
    }
}
