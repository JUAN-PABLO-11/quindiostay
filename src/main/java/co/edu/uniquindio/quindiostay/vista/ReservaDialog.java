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

import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.logica.ReservaService;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.modelo.PasajeroNuevo;
import co.edu.uniquindio.quindiostay.modelo.PlanVigente;
import co.edu.uniquindio.quindiostay.modelo.SalidaDisponible;
import co.edu.uniquindio.quindiostay.vista.comun.Formato;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;

/**
 * Formulario para crear una reserva a nombre de un cliente; 
 * Se escoge el cliente, el canal, el paquete, la salida (solo salidas abiertas con cupos) y 
 * el plan (solo los vigentes ese día), y se agregan los pasajeros. 
 * El valor se calcula solo (precio del plan x pasajeros). 
 * Los adultos se guardan primero.
 */
public class ReservaDialog extends JDialog{
    private final ReservaService servicio = new ReservaService();
    private boolean guardado = false;
    private boolean cargando = false;
    private final List<PasajeroNuevo> pasajeros = new ArrayList<>();

    private final JComboBox<Opcion> cmbCliente = new JComboBox<>();
    private final JComboBox<String> cmbCanal = new JComboBox<>(ReservaService.CANALES.toArray(String[]::new));
    private final JComboBox<Opcion> cmbPaquete = new JComboBox<>();
    private final JComboBox<SalidaDisponible> cmbSalida = new JComboBox<>();
    private final JComboBox<PlanVigente> cmbPlan = new JComboBox<>();
    private final DefaultTableModel modeloPasajeros = new DefaultTableModel(
            new String[] {"Nombre", "Documento", "Nacimiento", "Edad en la salida"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tablaPasajeros = new JTable(modeloPasajeros);
    private final JLabel lblTotal = new JLabel(" ");
    private final JLabel lblEstado = new JLabel(" ");

    /** Abre el formulario y devuelve true si se guardó. */
    public static boolean mostrar(Component padre) {
        ReservaDialog dialogo = new ReservaDialog(SwingUtilities.getWindowAncestor(padre));
        dialogo.setVisible(true);   // modal: se queda aquí hasta que se cierre
        return dialogo.guardado;
    }

    private ReservaDialog(Window padre) {
        super(padre, "Nueva reserva", ModalityType.APPLICATION_MODAL);

        Formulario f = new Formulario();
        f.campo("Cliente", cmbCliente);
        f.campo("Canal", cmbCanal);
        f.campo("Paquete", cmbPaquete);
        f.campo("Salida", cmbSalida);
        f.campo("Plan", cmbPlan);

        tablaPasajeros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPasajeros.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(tablaPasajeros);
        scroll.setPreferredSize(new Dimension(620, 140));

        JButton btnAgregar = new JButton("Agregar pasajero");
        JButton btnQuitar = new JButton("Quitar pasajero");
        btnAgregar.addActionListener(e -> agregarPasajero());
        btnQuitar.addActionListener(e -> quitarPasajero());
        JPanel barraPasajeros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        barraPasajeros.add(new JLabel("Pasajeros:"));
        barraPasajeros.add(btnAgregar);
        barraPasajeros.add(btnQuitar);

        lblTotal.putClientProperty("FlatLaf.styleClass", "h3");
        JPanel pieTabla = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pieTabla.add(lblTotal);

        JPanel detalle = new JPanel(new BorderLayout(0, 6));
        detalle.setBorder(BorderFactory.createEmptyBorder(8, 16, 0, 16));
        detalle.add(barraPasajeros, BorderLayout.NORTH);
        detalle.add(scroll, BorderLayout.CENTER);
        detalle.add(pieTabla, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(f, BorderLayout.NORTH);
        centro.add(detalle, BorderLayout.CENTER);

        JButton btnGuardar = new JButton("Crear reserva");
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
        cmbPaquete.addActionListener(e -> cargarSalidas());
        cmbSalida.addActionListener(e -> cargarPlanes());
        cmbPlan.addActionListener(e -> refrescarTotal());
        cmbCanal.setSelectedItem("mostrador");
        cmbCliente.setSelectedIndex(-1);
        cmbPaquete.setSelectedIndex(-1);
        refrescarPasajeros();

        pack();
        setLocationRelativeTo(padre);
    }

    private void cargarListas() {
        try {
            Formulario.llenar(cmbCliente, servicio.clientes());
            Formulario.llenar(cmbPaquete, servicio.paquetes());
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        }
    }

    /** Al elegir el paquete se cargan sus salidas abiertas (RN18). */
    private void cargarSalidas() {
        if (cargando) {
            return;
        }
        cargando = true;
        try {
            cmbSalida.removeAllItems();
            cmbPlan.removeAllItems();
            int idPaquete = Formulario.idSeleccionado(cmbPaquete);
            if (idPaquete > 0) {
                for (SalidaDisponible s : servicio.salidas(idPaquete)) {
                    cmbSalida.addItem(s);
                }
                cmbSalida.setSelectedIndex(-1);
                lblEstado.setText(cmbSalida.getItemCount() == 0 ? "Ese paquete no tiene salidas abiertas." : " ");
            }
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        } finally {
            cargando = false;
        }
        refrescarPasajeros();
    }

    /** Al elegir la salida se cargan los planes vigentes en su fecha (RN10). */
    private void cargarPlanes() {
        if (cargando) {
            return;
        }
        cargando = true;
        try {
            cmbPlan.removeAllItems();
            SalidaDisponible salida = (SalidaDisponible) cmbSalida.getSelectedItem();
            if (salida != null) {
                for (PlanVigente p : servicio.planes(Formulario.idSeleccionado(cmbPaquete), salida.fecha())) {
                    cmbPlan.addItem(p);
                }
                cmbPlan.setSelectedIndex(-1);
                lblEstado.setText(cmbPlan.getItemCount() == 0
                        ? "Esa salida no tiene planes activos y vigentes en su fecha." : " ");
            }
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        } finally {
            cargando = false;
        }
        refrescarPasajeros();
    }

    private void agregarPasajero() {
        PasajeroNuevo nuevo = PasajeroDialog.mostrar(this);
        if (nuevo == null) {
            return;
        }
        try {
            servicio.validarPasajero(nuevo);
            for (PasajeroNuevo p : pasajeros) {
                if (p.tipoDocumento().equals(nuevo.tipoDocumento()) && p.numeroDocumento().equals(nuevo.numeroDocumento())) {
                    lblEstado.setText("El pasajero con documento " + nuevo.numeroDocumento() + " ya está en la lista.");
                    return;
                }
            }
            pasajeros.add(nuevo);
            lblEstado.setText(" ");
            refrescarPasajeros();
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        }
    }

    private void quitarPasajero() {
        int fila = tablaPasajeros.getSelectedRow();
        if (fila < 0) {
            lblEstado.setText("Selecciona un pasajero para quitarlo.");
            return;
        }
        pasajeros.remove(fila);
        lblEstado.setText(" ");
        refrescarPasajeros();
    }

    /** Edad el día de la salida; si todavía no hay salida elegida, la edad de hoy. */
    private void refrescarPasajeros() {
        SalidaDisponible salida = (SalidaDisponible) cmbSalida.getSelectedItem();
        LocalDate referencia = salida == null ? LocalDate.now() : salida.fecha();
        modeloPasajeros.setRowCount(0);
        for (PasajeroNuevo p : pasajeros) {
            modeloPasajeros.addRow(new Object[] {
                    p.nombreCompleto(), p.tipoDocumento() + " " + p.numeroDocumento(),
                    Formato.fecha(p.fechaNacimiento()),
                    p.edadEn(referencia) + (p.esAdultoEn(referencia) ? "  (adulto)" : "  (menor)")
            });
        }
        refrescarTotal();
    }

    private void refrescarTotal() {
        PlanVigente plan = (PlanVigente) cmbPlan.getSelectedItem();
        lblTotal.setText("Valor total: " + Formato.pesos(ReservaService.valorDe(plan, pasajeros.size()))
                + "  (" + pasajeros.size() + (pasajeros.size() == 1 ? " pasajero)" : " pasajeros)"));
    }

    private void guardar() {
        try {
            int id = servicio.crear(
                    Formulario.idSeleccionado(cmbCliente),
                    (String) cmbCanal.getSelectedItem(),
                    (SalidaDisponible) cmbSalida.getSelectedItem(),
                    (PlanVigente) cmbPlan.getSelectedItem(),
                    pasajeros);
            lblEstado.setText(" ");
            guardado = true;
            javax.swing.JOptionPane.showMessageDialog(this, "Reserva " + id + " creada. Queda pendiente hasta el primer pago.",
                    "Reserva creada", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
            pack();
        }
    }
}
