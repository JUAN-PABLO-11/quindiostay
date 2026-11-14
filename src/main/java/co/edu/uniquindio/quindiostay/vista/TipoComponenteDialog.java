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
import co.edu.uniquindio.quindiostay.logica.TipoComponenteService;
import co.edu.uniquindio.quindiostay.modelo.TipoComponente;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;

/**
 * Formulario para crear o editar el tipo de componente.
 */
public class TipoComponenteDialog extends JDialog{
    private final TipoComponenteService servicio = new TipoComponenteService();
    private final TipoComponente original;   // null = nuevo
    private boolean guardado = false;

    private final JTextField txtNombre = new JTextField(25);
    private final JLabel lblEstado = new JLabel(" ");

    /** Abre el formulario y devuelve true si se guardó. */
    public static boolean mostrar(Component padre, TipoComponente tipoComponente) {
        TipoComponenteDialog dialogo = new TipoComponenteDialog(SwingUtilities.getWindowAncestor(padre), tipoComponente);
        dialogo.setVisible(true);   // modal: se queda aquí hasta que se cierre
        return dialogo.guardado;
    }

    private TipoComponenteDialog(Window padre, TipoComponente tipoComponente) {
        super(padre, tipoComponente == null ? "Nuevo tipo de componente" : "Editar tipo de componente", ModalityType.APPLICATION_MODAL);
        this.original = tipoComponente;

        Formulario f = new Formulario();
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

        if (tipoComponente != null) {
            txtNombre.setText(tipoComponente.nombre());
        }
        pack();
        setLocationRelativeTo(padre);
    }

    private TipoComponente construir() {
        return new TipoComponente(original == null ? null : original.id(), txtNombre.getText().trim());
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
