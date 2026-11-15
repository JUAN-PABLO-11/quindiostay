package co.edu.uniquindio.quindiostay.vista;

import javax.swing.JSpinner;
import javax.swing.JTextArea;
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
import co.edu.uniquindio.quindiostay.logica.PlanService;
import co.edu.uniquindio.quindiostay.logica.ReglaException;
import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.modelo.Plan;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;

/** Formulario para crear o editar un plan. Las fechas se escriben como aaaa-mm-dd. */
public class PlanDialog extends JDialog {
    private final PlanService servicio = new PlanService();
    private final Plan original;   // null = nuevo
    private boolean guardado = false;

    private final JTextField txtNombre = new JTextField(30);
    private final JTextArea txtDescripcion;
    private final JComboBox<String> cmbTipo = new JComboBox<>(PlanService.TIPOS.toArray(String[]::new));
    private final JSpinner spnPrecio =
            new JSpinner(new SpinnerNumberModel(500000, 1, PlanService.PRECIO_MAXIMO, 10000));
    private final JTextField txtInicio = new JTextField(10);
    private final JTextField txtFin = new JTextField(10);
    private final JComboBox<String> cmbEstadoPlan = new JComboBox<>(PlanService.ESTADOS.toArray(String[]::new));
    private final JComboBox<Opcion> cmbPaquete = new JComboBox<>();
    private final JLabel lblEstado = new JLabel(" ");

    /** Abre el formulario y devuelve true si se guardó. */
    public static boolean mostrar(Component padre, Plan plan) {
        PlanDialog dialogo = new PlanDialog(SwingUtilities.getWindowAncestor(padre), plan);
        dialogo.setVisible(true);   // modal: se queda aquí hasta que se cierre
        return dialogo.guardado;
    }

    private PlanDialog(Window padre, Plan plan) {
        super(padre, plan == null ? "Nuevo plan" : "Editar plan", ModalityType.APPLICATION_MODAL);
        this.original = plan;

        Formulario f = new Formulario();
        f.campo("Paquete", cmbPaquete);
        f.campo("Nombre", txtNombre);
        txtDescripcion = f.areaTexto("Descripción", 3);
        f.campo("Tipo de plan", cmbTipo);
        f.campo("Precio por persona ($)", spnPrecio);
        f.campo("Vigencia desde (aaaa-mm-dd)", txtInicio);
        f.campo("Vigencia hasta (aaaa-mm-dd)", txtFin);
        f.campo("Estado", cmbEstadoPlan);

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
        if (plan != null) {
            llenar(plan);
        } else {
            cmbTipo.setSelectedIndex(-1);
            txtInicio.setText(LocalDate.now().toString());
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

    private void llenar(Plan p) {
        txtNombre.setText(p.nombre());
        txtDescripcion.setText(p.descripcion());
        cmbTipo.setSelectedItem(p.tipoPlan());
        spnPrecio.setValue(Math.max(1, Math.min(p.precio(), PlanService.PRECIO_MAXIMO)));
        txtInicio.setText(p.fechaInicio().toString());
        txtFin.setText(p.fechaFin().toString());
        cmbEstadoPlan.setSelectedItem(p.estado());
        Formulario.seleccionar(cmbPaquete, p.idPaquete(), p.paquete());
    }

    /** Arma el objeto con lo que hay en los campos; 
     * lanza ReglaException si una fecha está mal escrita. 
     */
    private Plan construir() throws ReglaException {
        return new Plan(
                original == null ? null : original.id(),
                txtNombre.getText().trim(),
                txtDescripcion.getText().trim(),
                Formulario.textoSeleccionado(cmbTipo),
                (Integer) spnPrecio.getValue(),
                PlanService.parsearFecha("La fecha de inicio", txtInicio.getText()),
                PlanService.parsearFecha("La fecha fin", txtFin.getText()),
                Formulario.textoSeleccionado(cmbEstadoPlan),
                Formulario.idSeleccionado(cmbPaquete),
                Formulario.textoSeleccionado(cmbPaquete));
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
