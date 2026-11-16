package co.edu.uniquindio.quindiostay.vista;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.quindiostay.datos.CatalogoDAO;
import co.edu.uniquindio.quindiostay.logica.CotizacionService;
import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.modelo.Componente;
import co.edu.uniquindio.quindiostay.modelo.Cotizacion;
import co.edu.uniquindio.quindiostay.modelo.DetalleCotizacion;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.vista.comun.Formato;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;
import co.edu.uniquindio.quindiostay.vista.comun.SelectorFecha;

/**
 * Formulario maestro-detalle de una cotización: arriba los datos generales y abajo la tabla
 * de líneas (agregar, editar, quitar). El total se recalcula solo. 
 * Guardar escribe la cotización y todas sus líneas en una sola transacción.
 */
public class CotizacionDialog extends JDialog {
    private final CotizacionService servicio = new CotizacionService();
    private final Cotizacion original;   // null = nueva
    private boolean guardado = false;
    private final List<DetalleCotizacion> lineas = new ArrayList<>();
    private List<Componente> disponibles = new ArrayList<>();

    private final JTextField txtNumero = new JTextField(12);
    private final SelectorFecha selFecha = new SelectorFecha(LocalDate.now().getYear() - 2, LocalDate.now().getYear());
    private final JComboBox<String> cmbEstadoCot = new JComboBox<>(CotizacionService.ESTADOS.toArray(String[]::new));
    private final JComboBox<Opcion> cmbPrestador = new JComboBox<>();
    private final JComboBox<Opcion> cmbCliente = new JComboBox<>();
    private final DefaultTableModel modeloLineas = new DefaultTableModel(
            new String[] {"#", "Componente", "Fecha del servicio", "Personas", "Valor"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tablaLineas = new JTable(modeloLineas);
    private final JLabel lblTotal = new JLabel(" ");
    private final JLabel lblEstado = new JLabel(" ");

    /** Abre el formulario y devuelve true si se guardó. */
    public static boolean mostrar(Component padre, Cotizacion cotizacion) {
        CotizacionDialog dialogo = new CotizacionDialog(SwingUtilities.getWindowAncestor(padre), cotizacion);
        dialogo.setVisible(true);   // modal: se queda aquí hasta que se cierre
        return dialogo.guardado;
    }

    private CotizacionDialog(Window padre, Cotizacion cotizacion) {
        super(padre, cotizacion == null ? "Nueva cotización" : "Editar cotización " + cotizacion.numero(),
                ModalityType.APPLICATION_MODAL);
        this.original = cotizacion;

        txtNumero.setEditable(false);
        txtNumero.setText(cotizacion == null ? "Se asigna al guardar" : cotizacion.numero());

        Formulario f = new Formulario();
        f.campo("Número", txtNumero);
        f.campo("Fecha de la cotización", selFecha);
        f.campo("Estado", cmbEstadoCot);
        f.campo("Prestador que cotiza", cmbPrestador);
        f.campo("Cliente", cmbCliente);

        tablaLineas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaLineas.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(tablaLineas);
        scroll.setPreferredSize(new Dimension(640, 170));

        JButton btnAgregar = new JButton("Agregar línea");
        JButton btnEditarLinea = new JButton("Editar línea");
        JButton btnQuitar = new JButton("Quitar línea");
        btnAgregar.addActionListener(e -> agregarLinea());
        btnEditarLinea.addActionListener(e -> editarLinea());
        btnQuitar.addActionListener(e -> quitarLinea());

        JPanel barraLineas = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        barraLineas.add(new JLabel("Detalle:"));
        barraLineas.add(btnAgregar);
        barraLineas.add(btnEditarLinea);
        barraLineas.add(btnQuitar);

        lblTotal.putClientProperty("FlatLaf.styleClass", "h3");
        JPanel pieLineas = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pieLineas.add(lblTotal);

        JPanel detalle = new JPanel(new BorderLayout(0, 6));
        detalle.setBorder(BorderFactory.createEmptyBorder(8, 16, 0, 16));
        detalle.add(barraLineas, BorderLayout.NORTH);
        detalle.add(scroll, BorderLayout.CENTER);
        detalle.add(pieLineas, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(f, BorderLayout.NORTH);
        centro.add(detalle, BorderLayout.CENTER);

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
        if (cotizacion != null) {
            llenar(cotizacion);
        } else {
            selFecha.setFecha(LocalDate.now());
            cmbEstadoCot.setSelectedItem("borrador");
            cmbEstadoCot.setEnabled(false);   // toda cotización nueva nace en borrador
            cmbCliente.setSelectedIndex(-1);
        }
        // El encargado de prestador solo cotiza a nombre de su propio prestador
        if (Sesion.esEncargado()) {
            Formulario.seleccionar(cmbPrestador, Sesion.actual().idPrestador(), "");
            cmbPrestador.setEnabled(false);
        }
        refrescarLineas();
        pack();
        setLocationRelativeTo(padre);
    }

    private void cargarListas() {
        try {
            CatalogoDAO catalogo = new CatalogoDAO();
            Formulario.llenar(cmbPrestador, catalogo.prestadoresActivos());   // RN02
            Formulario.llenar(cmbCliente, catalogo.clientes());
            disponibles = servicio.componentesDisponibles();
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        }
    }

    /** Pone los datos de la cotización guardada, con sus líneas (modo editar). */
    private void llenar(Cotizacion c) {
        selFecha.setFecha(c.fecha());
        cmbEstadoCot.setSelectedItem(c.estado());
        Formulario.seleccionar(cmbPrestador, c.idPrestador(), c.prestador());
        Formulario.seleccionar(cmbCliente, c.idCliente(), c.cliente());
        cmbPrestador.setEnabled(false);   // el prestador emisor no se cambia
        try {
            lineas.addAll(servicio.lineas(c));
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        }
    }

    private void refrescarLineas() {
        modeloLineas.setRowCount(0);
        for (DetalleCotizacion d : lineas) {
            modeloLineas.addRow(new Object[] {
                    d.numeroLinea(), d.componente(), Formato.fecha(d.fechaServicio()),
                    d.cantidadPersonas(), Formato.pesos(d.valor())
            });
        }
        lblTotal.setText("Total: " + Formato.pesos(CotizacionService.totalDe(lineas)));
    }

    private void agregarLinea() {
        DetalleCotizacion nueva = DetalleCotizacionDialog.mostrar(this, null, disponibles, selFecha.getFecha());
        if (nueva != null) {
            lineas.add(new DetalleCotizacion(lineas.size() + 1, nueva.fechaServicio(), nueva.cantidadPersonas(),
                    nueva.valor(), nueva.idComponente(), nueva.componente()));
            lblEstado.setText(" ");
            refrescarLineas();
        }
    }

    private void editarLinea() {
        int fila = tablaLineas.getSelectedRow();
        if (fila < 0) {
            lblEstado.setText("Selecciona una línea para editarla.");
            return;
        }
        DetalleCotizacion editada = DetalleCotizacionDialog.mostrar(this, lineas.get(fila), disponibles, selFecha.getFecha());
        if (editada != null) {
            lineas.set(fila, editada);
            lblEstado.setText(" ");
            refrescarLineas();
        }
    }

    private void quitarLinea() {
        int fila = tablaLineas.getSelectedRow();
        if (fila < 0) {
            lblEstado.setText("Selecciona una línea para quitarla.");
            return;
        }
        lineas.remove(fila);
        // Se renumeran para que la tabla siempre muestre 1..n
        for (int i = 0; i < lineas.size(); i++) {
            DetalleCotizacion d = lineas.get(i);
            lineas.set(i, new DetalleCotizacion(i + 1, d.fechaServicio(), d.cantidadPersonas(), d.valor(),
                    d.idComponente(), d.componente()));
        }
        lblEstado.setText(" ");
        refrescarLineas();
    }

    private Cotizacion construir() {
        return new Cotizacion(
                original == null ? null : original.id(),
                original == null ? null : original.numero(),
                selFecha.getFecha(),
                CotizacionService.totalDe(lineas),
                Formulario.textoSeleccionado(cmbEstadoCot),
                Formulario.idSeleccionado(cmbPrestador),
                Formulario.textoSeleccionado(cmbPrestador),
                Formulario.idSeleccionado(cmbCliente),
                Formulario.textoSeleccionado(cmbCliente));
    }

    private void guardar() {
        try {
            servicio.guardar(construir(), lineas);
            guardado = true;
            dispose();
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
            pack();
        }
    }
}
