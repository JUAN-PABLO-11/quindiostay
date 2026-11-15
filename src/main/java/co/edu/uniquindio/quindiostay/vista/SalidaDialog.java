package co.edu.uniquindio.quindiostay.vista;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
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

import co.edu.uniquindio.quindiostay.datos.CatalogoDAO;
import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.logica.SalidaService;
import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.modelo.Salida;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;
import co.edu.uniquindio.quindiostay.vista.comun.SelectorFecha;

/** Formulario para crear o editar una salida. Al editar, el paquete queda fijo. */
public class SalidaDialog extends JDialog{
    private final SalidaService servicio = new SalidaService();
    private final Salida original;   // null = nueva
    private boolean guardado = false;

    private final JComboBox<Opcion> cmbPaquete = new JComboBox<>();
    private final SelectorFecha selFecha = new SelectorFecha(LocalDate.now().getYear(), LocalDate.now().getYear() + 3);
    private final JSpinner spnCupo = new JSpinner(new SpinnerNumberModel(20, 1, 60, 1));
    private final JComboBox<String> cmbEstadoSalida =
            new JComboBox<>(SalidaService.ESTADOS.toArray(String[]::new));
    private final JLabel lblEstado = new JLabel(" ");

    /** Abre el formulario y devuelve true si se guardó. */
    public static boolean mostrar(Component padre, Salida salida) {
        SalidaDialog dialogo = new SalidaDialog(SwingUtilities.getWindowAncestor(padre), salida);
        dialogo.setVisible(true);   // modal: se queda aquí hasta que se cierre
        return dialogo.guardado;
    }

    private SalidaDialog(Window padre, Salida salida) {
        super(padre, salida == null ? "Nueva salida" : "Editar salida", ModalityType.APPLICATION_MODAL);
        this.original = salida;

        Formulario f = new Formulario();
        f.campo("Paquete", cmbPaquete);
        f.campo("Fecha de inicio", selFecha);
        f.campo("Cupo máximo", spnCupo);
        f.campo("Estado", cmbEstadoSalida);

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
        if (salida != null) {
            Formulario.seleccionar(cmbPaquete, salida.idPaquete(), salida.paquete());
            selFecha.setFecha(salida.fechaInicio());
            spnCupo.setValue(salida.cupoMaximo());
            cmbEstadoSalida.setSelectedItem(salida.estado());
            cmbPaquete.setEnabled(false);   // el paquete de una salida no se cambia
        } else {
            cmbEstadoSalida.setSelectedItem("programada");
            cmbEstadoSalida.setEnabled(false);   // toda salida nueva nace programada
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

    private Salida construir() {
        return new Salida(
                original == null ? null : original.id(),
                selFecha.getFecha(),
                (Integer) spnCupo.getValue(),
                Formulario.textoSeleccionado(cmbEstadoSalida),
                Formulario.idSeleccionado(cmbPaquete),
                Formulario.textoSeleccionado(cmbPaquete),
                original == null ? 0 : original.ocupados());
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
