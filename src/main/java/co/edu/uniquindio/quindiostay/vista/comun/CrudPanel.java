package co.edu.uniquindio.quindiostay.vista.comun;

import co.edu.uniquindio.quindiostay.logica.ErroresBD;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Pantalla CRUD genérica: título, buscador, botones Nuevo / Editar / Eliminar,
 * la tabla y la barra inferior donde se muestran los mensajes y errores.
 *
 * Cada CRUD (PaquetesPanel, PrestadoresPanel...) hereda de esta clase y solo
 * implementa los 6 métodos abstractos. Al final de su constructor llama a cargar().
 *
 * @param <T> clase del modelo (Paquete, Prestador...)
 */
public abstract class CrudPanel<T> extends JPanel {

    protected final JTextField txtBuscar = new JTextField(22);
    protected final JButton btnNuevo = new JButton("Nuevo");
    protected final JButton btnEditar = new JButton("Editar");
    protected final JButton btnEliminar = new JButton("Eliminar");
    protected final JTable tabla;

    private final DefaultTableModel modeloTabla;
    private final JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private final JLabel lblEstado = new JLabel(" ");
    private List<T> datos = new ArrayList<>();

    protected CrudPanel(String titulo, String... columnas) {
        super(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.putClientProperty("FlatLaf.styleClass", "h2");

        JButton btnBuscar = new JButton("Buscar");
        barra.add(new JLabel("Buscar:"));
        barra.add(txtBuscar);
        barra.add(btnBuscar);
        barra.add(Box.createHorizontalStrut(20));
        barra.add(btnNuevo);
        barra.add(btnEditar);
        barra.add(btnEliminar);

        JPanel norte = new JPanel(new BorderLayout(0, 10));
        norte.add(lblTitulo, BorderLayout.NORTH);
        norte.add(barra, BorderLayout.CENTER);

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setRowHeight(26);

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(lblEstado, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> cargar());
        txtBuscar.addActionListener(e -> cargar());
        btnNuevo.addActionListener(e -> nuevo());
        btnEditar.addActionListener(e -> seleccionado().ifPresent(this::editar));
        btnEliminar.addActionListener(e -> seleccionado().ifPresent(this::confirmarEliminar));
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && btnEditar.isVisible()) {
                    seleccionado().ifPresent(CrudPanel.this::editar);
                }
            }
        });
    }

    // ------------------------------------------------------------------
    // Lo que implementa cada CRUD
    // ------------------------------------------------------------------

    /** Consulta los registros que coinciden con el texto del buscador. */
    protected abstract List<T> consultar(String filtro) throws Exception;

    /** Valores de las columnas de la tabla para un registro (mismo orden que las columnas). */
    protected abstract Object[] fila(T item);

    /** Abre el formulario vacío. */
    protected abstract void nuevo();

    /** Abre el formulario con los datos del registro. */
    protected abstract void editar(T item);

    /** Elimina el registro (la confirmación ya la hace esta clase). */
    protected abstract void eliminar(T item) throws Exception;

    /** Texto para la pregunta "¿Eliminar ...?", por ejemplo: el paquete "Ruta del café". */
    protected abstract String describir(T item);

    // ------------------------------------------------------------------
    // Lo que ya viene hecho
    // ------------------------------------------------------------------

    /** Vuelve a consultar y a llenar la tabla. */
    public final void cargar() {
        try {
            datos = consultar(txtBuscar.getText());
            modeloTabla.setRowCount(0);
            for (T item : datos) {
                modeloTabla.addRow(fila(item));
            }
            info(datos.size() + (datos.size() == 1 ? " registro" : " registros"));
        } catch (Exception e) {
            error(e);
        }
    }

    /** Registro seleccionado en la tabla (respeta el orden si el usuario ordenó por una columna). */
    protected Optional<T> seleccionado() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            info("Selecciona un registro de la tabla.");
            return Optional.empty();
        }
        return Optional.of(datos.get(tabla.convertRowIndexToModel(filaVista)));
    }

    /** Agrega un botón propio a la barra (por ejemplo "Factura" o "Pagar saldo"). */
    protected void agregarBoton(JButton boton) {
        barra.add(boton);
    }

    /** Oculta Nuevo / Editar / Eliminar (por ejemplo, para roles de solo consulta). */
    protected void soloLectura() {
        btnNuevo.setVisible(false);
        btnEditar.setVisible(false);
        btnEliminar.setVisible(false);
    }

    /** Mensaje normal en la barra inferior. */
    protected void info(String mensaje) {
        lblEstado.setForeground(UIManager.getColor("Label.foreground"));
        lblEstado.setText(mensaje);
    }

    /** Error en rojo en la barra inferior, ya traducido (ver ErroresBD). */
    protected void error(Throwable e) {
        lblEstado.setForeground(new java.awt.Color(0xC62828));
        lblEstado.setText(ErroresBD.mensaje(e));
    }

    private void confirmarEliminar(T item) {
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Eliminar " + describir(item) + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            eliminar(item);
            cargar();
            info("Se eliminó " + describir(item) + ".");
        } catch (Exception e) {
            error(e);
            JOptionPane.showMessageDialog(this, ErroresBD.mensaje(e), "No se pudo eliminar",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}
