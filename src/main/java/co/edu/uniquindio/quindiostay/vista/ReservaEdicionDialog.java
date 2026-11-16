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

import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.logica.ReservaService;
import co.edu.uniquindio.quindiostay.modelo.Reserva;
import co.edu.uniquindio.quindiostay.vista.comun.Formato;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;

/**
 * Edita una reserva existente.
 *  Solo se pueden cambiar el canal y el estado (pendiente, confirmada o completada); 
 * El cliente, el plan, la salida, los pasajeros y el valor quedan como se acordaron.
 * Para cancelar se usa el botón "Cancelar reserva" del listado (procedimiento cancelar_reserva).
 */
public class ReservaEdicionDialog extends JDialog{
    private final ReservaService servicio = new ReservaService();
    private final Reserva original;
    private boolean guardado = false;

    private final JComboBox<String> cmbCanal = new JComboBox<>(ReservaService.CANALES.toArray(String[]::new));
    private final JComboBox<String> cmbEstadoReserva =
            new JComboBox<>(ReservaService.ESTADOS_EDITABLES.toArray(String[]::new));
    private final JLabel lblEstado = new JLabel(" ");

    /** Abre el formulario y devuelve true si se guardó. */
    public static boolean mostrar(Component padre, Reserva reserva) {
        ReservaEdicionDialog dialogo = new ReservaEdicionDialog(SwingUtilities.getWindowAncestor(padre), reserva);
        dialogo.setVisible(true);   // modal: se queda aquí hasta que se cierre
        return dialogo.guardado;
    }

    private ReservaEdicionDialog(Window padre, Reserva reserva) {
        super(padre, "Editar reserva " + reserva.id(), ModalityType.APPLICATION_MODAL);
        this.original = reserva;

        Formulario f = new Formulario();
        f.campo("Cliente", soloLectura(reserva.cliente()));
        f.campo("Paquete", soloLectura(reserva.paquete()));
        f.campo("Plan", soloLectura(reserva.plan()));
        f.campo("Salida", soloLectura(Formato.fecha(reserva.fechaSalida())));
        f.campo("Pasajeros", soloLectura(String.valueOf(reserva.pasajeros())));
        f.campo("Valor total", soloLectura(Formato.pesos(reserva.valorTotal())));
        f.campo("Canal", cmbCanal);
        f.campo("Estado", cmbEstadoReserva);

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

        cmbCanal.setSelectedItem(reserva.canal());
        cmbEstadoReserva.setSelectedItem(reserva.estado());
        pack();
        setLocationRelativeTo(padre);
    }

    private JTextField soloLectura(String texto) {
        JTextField campo = new JTextField(texto, 28);
        campo.setEditable(false);
        return campo;
    }

    private void guardar() {
        try {
            servicio.actualizar(original, (String) cmbCanal.getSelectedItem(),
                    (String) cmbEstadoReserva.getSelectedItem());
            guardado = true;
            dispose();
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
            pack();
        }
    }
}
