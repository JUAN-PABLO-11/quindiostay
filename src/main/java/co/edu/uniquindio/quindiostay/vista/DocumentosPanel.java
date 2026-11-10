package co.edu.uniquindio.quindiostay.vista;

import co.edu.uniquindio.quindiostay.vista.comun.Formulario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Map;

/**
 * Plantillas de documentos del negocio para los usuarios internos:
 * se escoge el documento, se escribe el número (de reserva, de pago o de cliente) y se genera.
 */
public class DocumentosPanel extends JPanel {

    /** Plantilla, parámetro que recibe y qué número se le pide al usuario. */
    private record Plantilla(String nombre, String archivo, String parametro, String pide) {
        @Override
        public String toString() {
            return nombre;
        }
    }

    private static final Plantilla[] PLANTILLAS = {
        new Plantilla("Comprobante de reserva (con itinerario)", "comprobante_reserva", "ID_RESERVA", "N.º de reserva"),
        new Plantilla("Comprobante de pago", "comprobante_pago", "ID_PAGO", "N.º de pago"),
        new Plantilla("Factura de venta", "factura", "ID_RESERVA", "N.º de reserva"),
        new Plantilla("Autorización de viaje de menores", "autorizacion_menores", "ID_RESERVA", "N.º de reserva"),
        new Plantilla("Autorización de tratamiento de datos", "autorizacion_datos", "ID_CLIENTE", "N.º de cliente"),
    };

    private final JComboBox<Plantilla> cmbPlantilla = new JComboBox<>(PLANTILLAS);
    private final JTextField txtNumero = new JTextField(10);
    private final JLabel lblPide = new JLabel();
    private final JLabel lblEstado = new JLabel(" ");

    public DocumentosPanel() {
        super(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));

        JLabel titulo = new JLabel("Documentos del negocio");
        titulo.putClientProperty("FlatLaf.styleClass", "h2");
        JLabel ayuda = new JLabel("<html>Genera los documentos con los datos de la base. El número aparece en la primera "
                + "columna de cada pantalla de gestión (Reservas, Pagos, Clientes).</html>");
        ayuda.setForeground(new Color(0x5F6368));

        Formulario f = new Formulario();
        f.campo("Documento", cmbPlantilla);
        JPanel filaNumero = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaNumero.add(txtNumero);
        f.campo("Número", filaNumero);
        f.campo(" ", lblPide);

        JButton btnGenerar = new JButton("Generar documento");
        btnGenerar.addActionListener(e -> generar());
        txtNumero.addActionListener(e -> generar());
        cmbPlantilla.addActionListener(e -> actualizarAyuda());
        actualizarAyuda();

        JPanel norte = new JPanel(new BorderLayout(0, 6));
        norte.add(titulo, BorderLayout.NORTH);
        norte.add(ayuda, BorderLayout.CENTER);

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(f, BorderLayout.NORTH);
        JPanel boton = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        boton.add(btnGenerar);
        centro.add(boton, BorderLayout.CENTER);

        lblEstado.setForeground(new Color(0xC62828));
        add(norte, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(lblEstado, BorderLayout.SOUTH);
    }

    private void actualizarAyuda() {
        Plantilla p = (Plantilla) cmbPlantilla.getSelectedItem();
        lblPide.setText("Escribe el " + p.pide().toLowerCase());
    }

    private void generar() {
        Plantilla p = (Plantilla) cmbPlantilla.getSelectedItem();
        int numero;
        try {
            numero = Integer.parseInt(txtNumero.getText().trim());
        } catch (NumberFormatException e) {
            lblEstado.setText("Escribe un " + p.pide().toLowerCase() + " válido (solo números).");
            return;
        }
        lblEstado.setText(" ");
        VisorReporte.abrir(this, p.archivo(), Map.of(p.parametro(), numero), p.nombre() + " " + numero);
    }
}
