package co.edu.uniquindio.quindiostay.vista;

import co.edu.uniquindio.quindiostay.logica.CompraService;
import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.modelo.DatosPago;
import co.edu.uniquindio.quindiostay.modelo.EstadoCuenta;
import co.edu.uniquindio.quindiostay.modelo.ResultadoPago;
import co.edu.uniquindio.quindiostay.vista.comun.Formato;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * Pago en línea con una pasarela SIMULADA (tarjeta de crédito, débito o PSE).
 * Los datos de la tarjeta solo se validan: no se guardan.
 */
public class PagoDialog extends JDialog {

    private static final String[] METODOS = {"Tarjeta de crédito", "Tarjeta débito", "PSE"};
    private static final String[] CODIGOS = {"tarjeta de credito", "tarjeta debito", "pse"};

    private final CompraService servicio = new CompraService();
    private final int idReserva;
    private EstadoCuenta cuenta;
    private boolean pagado = false;

    private final JRadioButton rbSaldo = new JRadioButton();
    private final JRadioButton rbAnticipo = new JRadioButton();
    private final JComboBox<String> cmbMetodo = new JComboBox<>(METODOS);
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel datosMetodo = new JPanel(tarjetas);
    private final JTextField txtNumero = new JTextField(20);
    private final JTextField txtTitular = new JTextField(20);
    private final JTextField txtVence = new JTextField(6);
    private final JPasswordField txtCvv = new JPasswordField(4);
    private final JComboBox<String> cmbBanco = new JComboBox<>(new String[] {
            "Bancolombia", "Davivienda", "Banco de Bogotá", "BBVA Colombia", "Banco de Occidente", "Nequi", "Daviplata"});
    private final JLabel lblEstado = new JLabel(" ");

    /** Abre la pasarela para una reserva; devuelve true si quedó un pago aprobado. */
    public static boolean mostrar(Component padre, int idReserva) {
        PagoDialog d = new PagoDialog(padre, idReserva);
        if (d.cuenta == null) {
            return false;
        }
        d.setVisible(true);
        return d.pagado;
    }

    private PagoDialog(Component padre, int idReserva) {
        super(SwingUtilities.getWindowAncestor(padre), "Pagar reserva " + idReserva, ModalityType.APPLICATION_MODAL);
        this.idReserva = idReserva;
        try {
            cuenta = servicio.estadoCuenta(idReserva);
            if (cuenta.saldo() <= 0) {
                JOptionPane.showMessageDialog(padre, "La reserva " + idReserva + " ya está pagada.");
                cuenta = null;
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(padre, ErroresBD.mensaje(e), "Pago", JOptionPane.WARNING_MESSAGE);
            cuenta = null;
            return;
        }

        JLabel lblResumen = new JLabel("<html><b>Reserva N.º " + idReserva + "</b><br>Valor: " + Formato.pesos(cuenta.valorTotal())
                + " · Pagado: " + Formato.pesos(cuenta.pagado()) + " · <b>Saldo: " + Formato.pesos(cuenta.saldo()) + "</b></html>");
        lblResumen.setBorder(BorderFactory.createEmptyBorder(14, 16, 4, 16));

        long anticipo = Math.round(cuenta.valorTotal() * CompraService.ANTICIPO / 1000.0) * 1000;
        rbSaldo.setText("Pagar el saldo completo (" + Formato.pesos(cuenta.saldo()) + ")");
        rbAnticipo.setText("Pagar un anticipo del 30 % (" + Formato.pesos(anticipo) + ") y el resto después");
        rbSaldo.setSelected(true);
        rbAnticipo.setEnabled(cuenta.pagado() == 0 && anticipo < cuenta.saldo());
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbSaldo);
        grupo.add(rbAnticipo);
        JPanel montos = new JPanel(new GridLayout(2, 1));
        montos.add(rbSaldo);
        montos.add(rbAnticipo);

        Formulario tarjeta = new Formulario();
        txtNumero.putClientProperty("JTextField.placeholderText", "4111 1111 1111 1111");
        txtVence.putClientProperty("JTextField.placeholderText", "MM/AA");
        tarjeta.campo("Número de tarjeta", txtNumero);
        tarjeta.campo("Nombre en la tarjeta", txtTitular);
        tarjeta.campo("Vence", txtVence);
        tarjeta.campo("Código (CVV)", txtCvv);
        Formulario pse = new Formulario();
        pse.campo("Banco", cmbBanco);
        pse.campo(" ", new JLabel("Serás redirigido al banco (simulado) y el pago se aprueba al instante."));
        datosMetodo.add(tarjeta, "tarjeta");
        datosMetodo.add(pse, "pse");
        cmbMetodo.addActionListener(e -> tarjetas.show(datosMetodo, cmbMetodo.getSelectedIndex() == 2 ? "pse" : "tarjeta"));

        Formulario f = new Formulario();
        f.campo("Monto", montos);
        f.campo("Medio de pago", cmbMetodo);

        JLabel aviso = new JLabel("<html><small>Pasarela simulada para el proyecto: no se cobra nada y la tarjeta no se guarda. "
                + "Una tarjeta que termine en 0002 simula un rechazo del banco.</small></html>");
        aviso.setForeground(new Color(0x5F6368));
        aviso.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(f, BorderLayout.NORTH);
        centro.add(datosMetodo, BorderLayout.CENTER);
        centro.add(aviso, BorderLayout.SOUTH);

        JButton btnPagar = new JButton("Pagar");
        JButton btnDespues = new JButton("Pagar después");
        btnPagar.addActionListener(e -> pagar(anticipo));
        btnDespues.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnDespues);
        botones.add(btnPagar);
        lblEstado.setForeground(new Color(0xC62828));
        lblEstado.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        JPanel sur = new JPanel(new BorderLayout());
        sur.add(lblEstado, BorderLayout.CENTER);
        sur.add(botones, BorderLayout.SOUTH);

        add(lblResumen, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnPagar);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        pack();
        setLocationRelativeTo(padre);
    }

    private void pagar(long anticipo) {
        long monto = rbAnticipo.isSelected() ? anticipo : cuenta.saldo();
        DatosPago datos = new DatosPago(CODIGOS[cmbMetodo.getSelectedIndex()], monto, txtNumero.getText(),
                txtTitular.getText(), txtVence.getText(), new String(txtCvv.getPassword()),
                (String) cmbBanco.getSelectedItem());
        try {
            ResultadoPago r = servicio.pagar(idReserva, datos);
            if (!r.aprobado()) {
                lblEstado.setText("El banco rechazó el pago (referencia " + r.referencia() + "). Intenta con otro medio.");
                return;
            }
            pagado = true;
            int ver = JOptionPane.showConfirmDialog(this, "Pago aprobado por " + Formato.pesos(monto)
                    + "\nReferencia: " + r.referencia() + "\n\n¿Quieres ver el comprobante de pago?",
                    "Pago aprobado", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            dispose();
            if (ver == JOptionPane.YES_OPTION) {
                VisorReporte.abrir(getOwner(), "comprobante_pago", Map.of("ID_PAGO", r.idPago()), "Comprobante de pago " + r.idPago());
            }
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        }
    }
}
