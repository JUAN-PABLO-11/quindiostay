package co.edu.uniquindio.quindiostay.vista;

import javax.swing.JSpinner;
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

import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.logica.PagoService;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.modelo.Pago;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;

/**
 * Formulario para registrar un pago o cambiarle el estado. 
 * Es distinto de PagoDialog (la pasarela en línea del cliente). 
 * Al editar solo se cambian el estado y la referencia.
 * El monto, el método y la reserva son el registro del dinero. 
 * La referencia solo se habilita con tarjeta o PSE (RN22).
 */
public class PagoCrudDialog extends JDialog{
    private final PagoService servicio = new PagoService();
    private final Pago original;   // null = nuevo
    private boolean guardado = false;

    private final JComboBox<Opcion> cmbReserva = new JComboBox<>();
    private final JComboBox<String> cmbMetodo = new JComboBox<>(PagoService.METODOS.toArray(String[]::new));
    private final JSpinner spnMonto = new JSpinner(new SpinnerNumberModel(100000, 1, 1_000_000_000, 10000));
    private final JComboBox<String> cmbEstadoPago;
    private final JTextField txtReferencia = new JTextField(24);
    private final JLabel lblEstado = new JLabel(" ");

    /** Abre el formulario y devuelve true si se guardó. */
    public static boolean mostrar(Component padre, Pago pago) {
        PagoCrudDialog dialogo = new PagoCrudDialog(SwingUtilities.getWindowAncestor(padre), pago);
        dialogo.setVisible(true);   // modal: se queda aquí hasta que se cierre
        return dialogo.guardado;
    }

    private PagoCrudDialog(Window padre, Pago pago) {
        super(padre, pago == null ? "Registrar pago" : "Editar pago " + pago.id(), ModalityType.APPLICATION_MODAL);
        this.original = pago;
        this.cmbEstadoPago = new JComboBox<>((pago == null ? PagoService.ESTADOS_NUEVO : PagoService.ESTADOS_EDICION)
                .toArray(String[]::new));

        Formulario f = new Formulario();
        f.campo("Reserva", cmbReserva);
        f.campo("Método de pago", cmbMetodo);
        f.campo("Monto ($)", spnMonto);
        f.campo("Estado del pago", cmbEstadoPago);
        f.campo("Referencia de la transacción", txtReferencia);

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
        if (pago != null) {
            Formulario.seleccionar(cmbReserva, pago.idReserva(), pago.reserva());
            cmbMetodo.setSelectedItem(pago.metodoPago());
            spnMonto.setValue((int) Math.min(pago.monto(), 1_000_000_000L));
            cmbEstadoPago.setSelectedItem(pago.estadoPago());
            txtReferencia.setText(pago.referencia() == null ? "" : pago.referencia());
            // El dinero registrado no se reescribe
            cmbReserva.setEnabled(false);
            cmbMetodo.setEnabled(false);
            spnMonto.setEnabled(false);
        } else {
            cmbReserva.setSelectedIndex(-1);
            cmbMetodo.setSelectedItem("efectivo");
            cmbReserva.addActionListener(e -> proponerMonto());
        }
        cmbMetodo.addActionListener(e -> aplicarMetodo());
        aplicarMetodo();

        pack();
        setLocationRelativeTo(padre);
    }

    private void cargarListas() {
        try {
            if (original == null) {
                Formulario.llenar(cmbReserva, servicio.reservasPagables());
            }
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        }
    }

    /** Al elegir la reserva se propone como monto lo que falta por pagar. */
    private void proponerMonto() {
        int idReserva = Formulario.idSeleccionado(cmbReserva);
        if (idReserva <= 0) {
            return;
        }
        try {
            long saldo = servicio.saldoDe(idReserva);
            if (saldo > 0) {
                spnMonto.setValue((int) Math.min(saldo, 1_000_000_000L));
            }
            lblEstado.setText(saldo > 0 ? " " : "Esa reserva ya está pagada por completo.");
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        }
    }

    /** RN22: la referencia solo se escribe en pagos con tarjeta o PSE. */
    private void aplicarMetodo() {
        boolean enLinea = PagoService.esEnLinea(Formulario.textoSeleccionado(cmbMetodo));
        txtReferencia.setEnabled(enLinea);
        if (!enLinea) {
            txtReferencia.setText("");
        }
    }

    private Pago construir() {
        String referencia = txtReferencia.isEnabled() ? txtReferencia.getText().trim() : "";
        return new Pago(
                original == null ? null : original.id(),
                original == null ? null : original.fechaPago(),
                ((Number) spnMonto.getValue()).longValue(),
                Formulario.textoSeleccionado(cmbMetodo),
                Formulario.textoSeleccionado(cmbEstadoPago),
                Formulario.idSeleccionado(cmbReserva),
                Formulario.textoSeleccionado(cmbReserva),
                referencia.isEmpty() ? null : referencia);
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
