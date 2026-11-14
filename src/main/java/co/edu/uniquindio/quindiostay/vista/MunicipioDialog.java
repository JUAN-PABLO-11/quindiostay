package co.edu.uniquindio.quindiostay.vista;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import co.edu.uniquindio.quindiostay.logica.MunicipioService;
import co.edu.uniquindio.quindiostay.modelo.Municipio;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;

/** Formulario para crear o editar un municipio. */
public class MunicipioDialog extends JDialog {
    private final MunicipioService servicio = new MunicipioService();
    private final Municipio original;   // null = nuevo
    private boolean guardado = false;

    private final JTextField txtCodigo = new JTextField(8);
    private final JTextField txtNombre = new JTextField(25);
    private final JLabel lblEstado = new JLabel(" ");

    /** Abre el formulario y devuelve true si se guardó. */
    public static boolean mostrar(Component padre, Municipio municipio) {
        MunicipioDialog dialogo = new MunicipioDialog(SwingUtilities.getWindowAncestor(padre), municipio);
        dialogo.setVisible(true);   // modal: se queda aquí hasta que se cierre
        return dialogo.guardado;
    }

    private MunicipioDialog(Window padre, Municipio municipio) {
        super(padre, municipio == null ? "Nuevo municipio" : "Editar municipio", ModalityType.APPLICATION_MODAL);
        this.original = municipio;

        Formulario f = new Formulario();
        f.campo("Código DANE (5 dígitos)", txtCodigo);
        f.campo("Nombre", txtNombre);

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

        if (municipio != null) {
            txtCodigo.setText(municipio.codigoDane());
            txtNombre.setText(municipio.nombre());
        }
        pack();
        setLocationRelativeTo(padre);
    }

    private Municipio construir() {
        return new Municipio(original == null ? null : original.id(),
                txtCodigo.getText().trim(), txtNombre.getText().trim());
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
