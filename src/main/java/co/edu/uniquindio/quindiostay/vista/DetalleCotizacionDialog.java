package co.edu.uniquindio.quindiostay.vista;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import co.edu.uniquindio.quindiostay.logica.CotizacionService;
import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.modelo.Componente;
import co.edu.uniquindio.quindiostay.modelo.DetalleCotizacion;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.vista.comun.Formato;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;
import co.edu.uniquindio.quindiostay.vista.comun.SelectorFecha;

/**
 * Formulario de una línea de la cotización. 
 * El valor se sugiere como costo x personas; se puede ajustar antes de aceptar.
 * No toca la base de datos: devuelve la línea y la cotización la guarda junto con las demás.
 */
public class DetalleCotizacionDialog extends JDialog{
    private final CotizacionService servicio = new CotizacionService();
    private final LocalDate fechaCotizacion;
    private final Map<Integer, Componente> componentes = new LinkedHashMap<>();
    private final int numeroLinea;
    private DetalleCotizacion resultado;

    private final JComboBox<Opcion> cmbComponente = new JComboBox<>();
    private final JLabel lblCosto = new JLabel(" ");
    private final SelectorFecha selFecha;
    private final JSpinner spnPersonas = new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));
    private final JSpinner spnValor = new JSpinner(new SpinnerNumberModel(100000, 1,
            CotizacionService.VALOR_MAXIMO_LINEA, 10000));
    private final JLabel lblEstado = new JLabel(" ");

    /**
     * Devuelve la línea escrita, o null si se canceló.
     * @param linea           línea a editar, o null para una nueva
     * @param disponibles     componentes que se pueden cotizar
     * @param fechaCotizacion fecha de la cotización (la línea debe ser posterior: RN25)
     */
    public static DetalleCotizacion mostrar(Component padre, DetalleCotizacion linea,
                                            List<Componente> disponibles, LocalDate fechaCotizacion) {
        DetalleCotizacionDialog d = new DetalleCotizacionDialog(SwingUtilities.getWindowAncestor(padre),
                linea, disponibles, fechaCotizacion);
        d.setVisible(true);   // modal: se queda aquí hasta que se cierre
        return d.resultado;
    }

    private DetalleCotizacionDialog(Window padre, DetalleCotizacion linea, List<Componente> disponibles,
                                    LocalDate fechaCotizacion) {
        super(padre, linea == null ? "Nueva línea" : "Editar línea", ModalityType.APPLICATION_MODAL);
        this.fechaCotizacion = fechaCotizacion;
        this.numeroLinea = linea == null ? 0 : linea.numeroLinea();
        this.selFecha = new SelectorFecha(fechaCotizacion.getYear(), fechaCotizacion.getYear() + 3);

        List<Opcion> opciones = new java.util.ArrayList<>();
        for (Componente c : disponibles) {
            componentes.put(c.id(), c);
            opciones.add(new Opcion(c.id(), c.nombre() + " — " + c.prestador()));
        }
        Formulario.llenar(cmbComponente, opciones);

        Formulario f = new Formulario();
        f.campo("Componente", cmbComponente);
        f.campo("Costo por persona", lblCosto);
        f.campo("Fecha del servicio", selFecha);
        f.campo("Cantidad de personas", spnPersonas);
        f.campo("Valor de la línea ($)", spnValor);

        JButton btnAceptar = new JButton("Aceptar");
        JButton btnCancelar = new JButton("Cancelar");
        btnAceptar.addActionListener(e -> aceptar());
        btnCancelar.addActionListener(e -> dispose());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnCancelar);
        botones.add(btnAceptar);

        lblEstado.setForeground(new Color(0xC62828));
        lblEstado.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        JPanel sur = new JPanel(new BorderLayout());
        sur.add(lblEstado, BorderLayout.CENTER);
        sur.add(botones, BorderLayout.SOUTH);

        add(f, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnAceptar);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        if (linea != null) {
            Formulario.seleccionar(cmbComponente, linea.idComponente(), linea.componente());
            selFecha.setFecha(linea.fechaServicio());
            spnPersonas.setValue(linea.cantidadPersonas());
            spnValor.setValue(linea.valor());
            mostrarCosto();
        } else {
            selFecha.setFecha(fechaCotizacion.plusDays(1));   // RN25: posterior a la cotización
            sugerirValor();
        }
        cmbComponente.addActionListener(e -> sugerirValor());
        spnPersonas.addChangeListener(e -> sugerirValor());

        pack();
        setLocationRelativeTo(padre);
    }

    private Componente componenteElegido() {
        return componentes.get(Formulario.idSeleccionado(cmbComponente));
    }

    private void mostrarCosto() {
        Componente c = componenteElegido();
        lblCosto.setText(c == null ? " " : Formato.pesos(c.costo()));
    }

    /** Pone el valor sugerido (costo x personas) cuando cambia el componente o las personas. */
    private void sugerirValor() {
        mostrarCosto();
        Componente c = componenteElegido();
        if (c == null) {
            return;
        }
        long sugerido = CotizacionService.valorSugerido(c, (Integer) spnPersonas.getValue());
        spnValor.setValue((int) Math.max(1, Math.min(sugerido, CotizacionService.VALOR_MAXIMO_LINEA)));
    }

    private void aceptar() {
        try {
            DetalleCotizacion linea = new DetalleCotizacion(
                    numeroLinea,
                    selFecha.getFecha(),
                    (Integer) spnPersonas.getValue(),
                    (Integer) spnValor.getValue(),
                    Formulario.idSeleccionado(cmbComponente),
                    Formulario.textoSeleccionado(cmbComponente));
            servicio.validarLinea(linea, fechaCotizacion);
            resultado = linea;
            dispose();
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
            pack();
        }
    }
}
