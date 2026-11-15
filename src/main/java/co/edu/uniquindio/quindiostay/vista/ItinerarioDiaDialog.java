package co.edu.uniquindio.quindiostay.vista;

import javax.swing.JSpinner;
import javax.swing.JTextArea;
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

import co.edu.uniquindio.quindiostay.datos.CatalogoDAO;
import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.logica.ItinerarioDiaService;
import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.modelo.ItinerarioDia;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;

/** Formulario para crear o editar un día del itinerario. 
 * Al editar, paquete y día quedan fijos por que son la llave. 
 */
public class ItinerarioDiaDialog extends JDialog {
    private final ItinerarioDiaService servicio = new ItinerarioDiaService();
    private final ItinerarioDia original;   // null = nuevo
    private boolean guardado = false;

    private final JComboBox<Opcion> cmbPaquete = new JComboBox<>();
    private final JSpinner spnDia = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));
    private final JTextArea txtDescripcion;
    private final JLabel lblEstado = new JLabel(" ");

    /** Abre el formulario y devuelve true si se guardó. */
    public static boolean mostrar(Component padre, ItinerarioDia dia) {
        ItinerarioDiaDialog dialogo = new ItinerarioDiaDialog(SwingUtilities.getWindowAncestor(padre), dia);
        dialogo.setVisible(true);   // modal: se queda aquí hasta que se cierre
        return dialogo.guardado;
    }

    private ItinerarioDiaDialog(Window padre, ItinerarioDia dia) {
        super(padre, dia == null ? "Nuevo día del itinerario" : "Editar día del itinerario",
                ModalityType.APPLICATION_MODAL);
        this.original = dia;

        Formulario f = new Formulario();
        f.campo("Paquete", cmbPaquete);
        f.campo("Número de día", spnDia);
        txtDescripcion = f.areaTexto("Qué se hace ese día", 4);

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
        if (dia != null) {
            Formulario.seleccionar(cmbPaquete, dia.idPaquete(), dia.paquete());
            spnDia.setValue(dia.numeroDia());
            txtDescripcion.setText(dia.descripcion());
            cmbPaquete.setEnabled(false);   // la llave primaria no se edita
            spnDia.setEnabled(false);
        }
        pack();
        setLocationRelativeTo(padre);
    }

    private void cargarListas() {
        try {
            Integer soloPrestador = Sesion.esEncargado() ? Sesion.actual().idPrestador() : null;
            Formulario.llenar(cmbPaquete, new CatalogoDAO().paquetes(soloPrestador));
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        }
    }

    private ItinerarioDia construir() {
        return new ItinerarioDia(
                Formulario.idSeleccionado(cmbPaquete),
                Formulario.textoSeleccionado(cmbPaquete),
                (Integer) spnDia.getValue(),
                txtDescripcion.getText().trim());
    }

    private void guardar() {
        try {
            servicio.guardar(construir(), original == null);
            guardado = true;
            dispose();
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
            pack();
        }
    }
}
