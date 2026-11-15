package co.edu.uniquindio.quindiostay.vista;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import co.edu.uniquindio.quindiostay.logica.PaqueteComponenteService;
import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;

/**
 * Asigna componentes a un paquete con casillas de verificación
 * Al cambiar de paquete se cargan sus componentes actuales.
 */
public class ComponentesPaqueteDialog extends JDialog {
    private final PaqueteComponenteService servicio = new PaqueteComponenteService();
    private boolean guardado = false;

    private final JComboBox<Opcion> cmbPaquete = new JComboBox<>();
    private final JPanel panelCasillas = new JPanel();
    private final Map<Integer, JCheckBox> casillas = new LinkedHashMap<>();
    private final JLabel lblContador = new JLabel(" ");
    private final JLabel lblEstado = new JLabel(" ");

    /**
     * Abre el formulario y devuelve true si se guardó.
     * @param idPaquete paquete preseleccionado (y fijo); null para elegirlo en el combo
     * @param nombrePaquete nombre para mostrarlo si el paquete no está en la lista
     */
    public static boolean mostrar(Component padre, Integer idPaquete, String nombrePaquete) {
        ComponentesPaqueteDialog dialogo =
                new ComponentesPaqueteDialog(SwingUtilities.getWindowAncestor(padre), idPaquete, nombrePaquete);
        dialogo.setVisible(true);   // modal: se queda aquí hasta que se cierre
        return dialogo.guardado;
    }

    private ComponentesPaqueteDialog(Window padre, Integer idPaquete, String nombrePaquete) {
        super(padre, "Componentes del paquete", ModalityType.APPLICATION_MODAL);

        Formulario f = new Formulario();
        f.campo("Paquete", cmbPaquete);

        panelCasillas.setLayout(new BoxLayout(panelCasillas, BoxLayout.Y_AXIS));
        panelCasillas.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        JScrollPane scroll = new JScrollPane(panelCasillas);
        scroll.setPreferredSize(new Dimension(460, 300));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(f, BorderLayout.NORTH);
        JPanel medio = new JPanel(new BorderLayout());
        medio.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        medio.add(new JLabel("Marca los componentes que incluye el paquete:"), BorderLayout.NORTH);
        medio.add(scroll, BorderLayout.CENTER);
        medio.add(lblContador, BorderLayout.SOUTH);
        centro.add(medio, BorderLayout.CENTER);

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

        add(centro, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnGuardar);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        cargarListas();
        if (idPaquete != null) {
            Formulario.seleccionar(cmbPaquete, idPaquete, nombrePaquete == null ? "" : nombrePaquete);
            cmbPaquete.setEnabled(false);
        }
        cmbPaquete.addActionListener(e -> marcarActuales());
        marcarActuales();

        pack();
        setLocationRelativeTo(padre);
    }

    /** Llena el combo de paquetes y arma una casilla por cada componente existente. */
    private void cargarListas() {
        try {
            CatalogoDAO catalogo = new CatalogoDAO();
            Integer soloPrestador = Sesion.esEncargado() ? Sesion.actual().idPrestador() : null;
            Formulario.llenar(cmbPaquete, catalogo.paquetes(soloPrestador));

            List<Opcion> componentes = catalogo.componentes();
            for (Opcion c : componentes) {
                JCheckBox casilla = new JCheckBox(c.texto());
                casilla.addActionListener(e -> actualizarContador());
                casillas.put(c.id(), casilla);
                panelCasillas.add(casilla);
            }
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        }
    }

    /** Marca solo los componentes que el paquete elegido ya tiene. */
    private void marcarActuales() {
        int idPaquete = Formulario.idSeleccionado(cmbPaquete);
        Set<Integer> actuales = Set.of();
        try {
            if (idPaquete > 0) {
                actuales = servicio.componentesDe(idPaquete);
            }
            lblEstado.setText(" ");
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        }
        for (Map.Entry<Integer, JCheckBox> par : casillas.entrySet()) {
            par.getValue().setSelected(actuales.contains(par.getKey()));
        }
        actualizarContador();
    }

    private void actualizarContador() {
        long n = casillas.values().stream().filter(JCheckBox::isSelected).count();
        lblContador.setText(n + (n == 1 ? " componente marcado" : " componentes marcados"));
    }

    private void guardar() {
        try {
            Set<Integer> marcados = new LinkedHashSet<>();
            casillas.forEach((id, casilla) -> {
                if (casilla.isSelected()) {
                    marcados.add(id);
                }
            });
            servicio.asignar(Formulario.idSeleccionado(cmbPaquete), marcados);
            guardado = true;
            dispose();
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
            pack();
        }
    }
}
