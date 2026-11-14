package co.edu.uniquindio.quindiostay.vista;

import javax.swing.JTextArea;
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
import co.edu.uniquindio.quindiostay.logica.DestinoService;
import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.modelo.Destino;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;

/** Formulario para crear o editar un destino. */
public class DestinoDialog extends JDialog {
    private final DestinoService servicio = new DestinoService();
    private final Destino original;   // null = nuevo
    private boolean guardado = false;

    private final JTextField txtNombre = new JTextField(30);
    private final JTextArea txtDescripcion;
    private final JComboBox<Opcion> cmbMunicipio = new JComboBox<>();
    private final JLabel lblEstado = new JLabel(" ");

    /** Abre el formulario y devuelve true si se guardó. */
    public static boolean mostrar(Component padre, Destino destino) {
        DestinoDialog dialogo = new DestinoDialog(SwingUtilities.getWindowAncestor(padre), destino);
        dialogo.setVisible(true);   // modal: se queda aquí hasta que se cierre
        return dialogo.guardado;
    }

    private DestinoDialog(Window padre, Destino destino) {
        super(padre, destino == null ? "Nuevo destino" : "Editar destino", ModalityType.APPLICATION_MODAL);
        this.original = destino;

        Formulario f = new Formulario();
        f.campo("Nombre", txtNombre);
        txtDescripcion = f.areaTexto("Descripción (opcional)", 4);
        f.campo("Municipio", cmbMunicipio);

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
        if (destino != null) {
            llenar(destino);
        }
        pack();
        setLocationRelativeTo(padre);
    }

    private void cargarListas() {
        try {
            Formulario.llenar(cmbMunicipio, new CatalogoDAO().municipios());
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        }
    }

    /** Pone los datos del destino en los campos (modo editar). */
    private void llenar(Destino d) {
        txtNombre.setText(d.nombre());
        txtDescripcion.setText(d.descripcion() == null ? "" : d.descripcion());
        Formulario.seleccionar(cmbMunicipio, d.idMunicipio(), d.municipio());
    }

    /** Arma el objeto con lo que hay en los campos (descripción vacía = null). */
    private Destino construir() {
        String descripcion = txtDescripcion.getText().trim();
        return new Destino(
                original == null ? null : original.id(),
                txtNombre.getText().trim(),
                descripcion.isEmpty() ? null : descripcion,
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
