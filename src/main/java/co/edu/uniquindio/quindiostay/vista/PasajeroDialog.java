package co.edu.uniquindio.quindiostay.vista;

import co.edu.uniquindio.quindiostay.logica.CompraService;
import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.modelo.PasajeroNuevo;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;

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
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Formulario para agregar un pasajero a la reserva. */
public class PasajeroDialog extends JDialog {

    private final CompraService servicio = new CompraService();
    private PasajeroNuevo resultado;

    private final JTextField txtNombres = new JTextField(24);
    private final JTextField txtApellidos = new JTextField(24);
    private final JComboBox<String> cmbTipo = new JComboBox<>(CompraService.TIPOS_DOCUMENTO_PASAJERO.toArray(String[]::new));
    private final JTextField txtDocumento = new JTextField(24);
    private final JTextField txtNacimiento = new JTextField(24);
    private final JLabel lblEstado = new JLabel(" ");

    /** Devuelve el pasajero escrito, o null si se canceló. */
    public static PasajeroNuevo mostrar(Component padre) {
        PasajeroDialog d = new PasajeroDialog(padre);
        d.setVisible(true);
        return d.resultado;
    }

    private PasajeroDialog(Component padre) {
        super(SwingUtilities.getWindowAncestor(padre), "Agregar pasajero", ModalityType.APPLICATION_MODAL);
        txtNacimiento.putClientProperty("JTextField.placeholderText", "aaaa-mm-dd");

        Formulario f = new Formulario();
        f.campo("Nombres", txtNombres);
        f.campo("Apellidos", txtApellidos);
        f.campo("Tipo de documento", cmbTipo);
        f.campo("Número de documento", txtDocumento);
        f.campo("Fecha de nacimiento", txtNacimiento);
        JLabel nota = new JLabel("<html><small>RC: registro civil · TI: tarjeta de identidad · CC: cédula · CE: cédula de extranjería</small></html>");
        nota.setForeground(new Color(0x5F6368));
        f.campo(" ", nota);

        JButton btnAgregar = new JButton("Agregar");
        JButton btnCancelar = new JButton("Cancelar");
        btnAgregar.addActionListener(e -> agregar());
        btnCancelar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnCancelar);
        botones.add(btnAgregar);

        lblEstado.setForeground(new Color(0xC62828));
        lblEstado.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        JPanel sur = new JPanel(new BorderLayout());
        sur.add(lblEstado, BorderLayout.CENTER);
        sur.add(botones, BorderLayout.SOUTH);

        add(f, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnAgregar);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        pack();
        setLocationRelativeTo(padre);
    }

    private void agregar() {
        LocalDate nacimiento;
        try {
            nacimiento = LocalDate.parse(txtNacimiento.getText().trim());
        } catch (DateTimeParseException e) {
            lblEstado.setText("Escribe la fecha de nacimiento como aaaa-mm-dd, por ejemplo 2015-08-21.");
            return;
        }
        PasajeroNuevo p = new PasajeroNuevo(txtNombres.getText().trim(), txtApellidos.getText().trim(),
                (String) cmbTipo.getSelectedItem(), txtDocumento.getText().trim(), nacimiento);
        try {
            servicio.validarPasajero(p);
            resultado = p;
            dispose();
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        }
    }
}
