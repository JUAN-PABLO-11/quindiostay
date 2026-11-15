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

import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.logica.PasajeroService;
import co.edu.uniquindio.quindiostay.modelo.Pasajero;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;
import co.edu.uniquindio.quindiostay.vista.comun.SelectorFecha;

/** Formulario para crear o editar un pasajero. */
public class PasajeroCrudDialog extends JDialog{
    private final PasajeroService servicio = new PasajeroService();
    private final Pasajero original;   // null = nuevo
    private boolean guardado = false;

    private final JTextField txtNombres = new JTextField(25);
    private final JTextField txtApellidos = new JTextField(25);
    private final JComboBox<String> cmbTipoDoc =
            new JComboBox<>(PasajeroService.TIPOS_DOCUMENTO.toArray(String[]::new));
    private final JTextField txtDocumento = new JTextField(15);
    private final SelectorFecha selNacimiento = new SelectorFecha(1900, LocalDate.now().getYear());
    private final JLabel lblEstado = new JLabel(" ");

    /** Abre el formulario y devuelve true si se guardó. */
    public static boolean mostrar(Component padre, Pasajero pasajero) {
        PasajeroCrudDialog dialogo = new PasajeroCrudDialog(SwingUtilities.getWindowAncestor(padre), pasajero);
        dialogo.setVisible(true);   // modal: se queda aquí hasta que se cierre
        return dialogo.guardado;
    }

    private PasajeroCrudDialog(Window padre, Pasajero pasajero) {
        super(padre, pasajero == null ? "Nuevo pasajero" : "Editar pasajero", ModalityType.APPLICATION_MODAL);
        this.original = pasajero;

        Formulario f = new Formulario();
        f.campo("Nombres", txtNombres);
        f.campo("Apellidos", txtApellidos);
        f.campo("Tipo de documento", cmbTipoDoc);
        f.campo("Número de documento", txtDocumento);
        f.campo("Fecha de nacimiento", selNacimiento);

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

        if (pasajero != null) {
            txtNombres.setText(pasajero.nombres());
            txtApellidos.setText(pasajero.apellidos());
            cmbTipoDoc.setSelectedItem(pasajero.tipoDocumento());
            txtDocumento.setText(pasajero.numeroDocumento());
            selNacimiento.setFecha(pasajero.fechaNacimiento());
        } else {
            selNacimiento.setFecha(LocalDate.of(1990, 1, 1));
        }
        pack();
        setLocationRelativeTo(padre);
    }

    private Pasajero construir() {
        return new Pasajero(
                original == null ? null : original.id(),
                txtNombres.getText().trim(),
                txtApellidos.getText().trim(),
                (String) cmbTipoDoc.getSelectedItem(),
                txtDocumento.getText().trim(),
                selNacimiento.getFecha());
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
