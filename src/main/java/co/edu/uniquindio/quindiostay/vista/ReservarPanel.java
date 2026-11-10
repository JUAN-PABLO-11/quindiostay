package co.edu.uniquindio.quindiostay.vista;

import co.edu.uniquindio.quindiostay.logica.CompraService;
import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.modelo.PasajeroNuevo;
import co.edu.uniquindio.quindiostay.modelo.PlanVigente;
import co.edu.uniquindio.quindiostay.modelo.SalidaDisponible;
import co.edu.uniquindio.quindiostay.vista.comun.Formato;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reserva en línea del cliente: paquete → salida → plan → pasajeros → total → pagar.
 */
public class ReservarPanel extends JPanel {

    private final CompraService servicio = new CompraService();
    private final JComboBox<Opcion> cmbPaquete = new JComboBox<>();
    private final JComboBox<SalidaDisponible> cmbSalida = new JComboBox<>();
    private final JComboBox<PlanVigente> cmbPlan = new JComboBox<>();
    private final JLabel lblPlan = new JLabel(" ");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[] {"Pasajero", "Documento", "Nacimiento", "Edad al viajar"}, 0) {
        @Override
        public boolean isCellEditable(int f, int c) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final List<PasajeroNuevo> pasajeros = new ArrayList<>();
    private final JLabel lblTotal = new JLabel();
    private final JLabel lblEstado = new JLabel(" ");
    private boolean cargando = false;

    public ReservarPanel() {
        super(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));

        JLabel titulo = new JLabel("Reservar un viaje");
        titulo.putClientProperty("FlatLaf.styleClass", "h2");

        Formulario f = new Formulario();
        f.campo("Paquete", cmbPaquete);
        f.campo("Salida", cmbSalida);
        f.campo("Plan", cmbPlan);
        lblPlan.setForeground(new Color(0x5F6368));
        f.campo(" ", lblPlan);

        JButton btnYo = new JButton("Agregarme como pasajero");
        JButton btnAgregar = new JButton("Agregar pasajero");
        JButton btnQuitar = new JButton("Quitar");
        btnYo.addActionListener(e -> agregarme());
        btnAgregar.addActionListener(e -> {
            PasajeroNuevo p = PasajeroDialog.mostrar(this);
            if (p != null) {
                agregar(p);
            }
        });
        btnQuitar.addActionListener(e -> quitar());

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(600, 170));

        JPanel barraPasajeros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JLabel lblPas = new JLabel("Pasajeros");
        lblPas.putClientProperty("FlatLaf.styleClass", "h4");
        barraPasajeros.add(lblPas);
        barraPasajeros.add(btnYo);
        barraPasajeros.add(btnAgregar);
        barraPasajeros.add(btnQuitar);

        JPanel zonaPasajeros = new JPanel(new BorderLayout(0, 6));
        zonaPasajeros.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        zonaPasajeros.add(barraPasajeros, BorderLayout.NORTH);
        zonaPasajeros.add(scroll, BorderLayout.CENTER);

        lblTotal.putClientProperty("FlatLaf.styleClass", "h3");
        JButton btnReservar = new JButton("Reservar y pagar");
        btnReservar.putClientProperty("JButton.buttonType", "default");
        btnReservar.addActionListener(e -> reservar());
        JPanel pie = new JPanel(new BorderLayout());
        pie.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        pie.add(lblTotal, BorderLayout.WEST);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.add(btnReservar);
        pie.add(derecha, BorderLayout.EAST);

        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.add(f, BorderLayout.NORTH);
        centro.add(zonaPasajeros, BorderLayout.CENTER);
        centro.add(pie, BorderLayout.SOUTH);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(lblEstado, BorderLayout.CENTER);

        add(titulo, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);

        cmbPaquete.addActionListener(e -> cargarSalidas());
        cmbSalida.addActionListener(e -> cargarPlanes());
        cmbPlan.addActionListener(e -> actualizarTotal());
        cargarPaquetes();
    }

    private void cargarPaquetes() {
        try {
            cargando = true;
            Formulario.llenar(cmbPaquete, servicio.paquetes());
        } catch (Exception e) {
            error(e);
        } finally {
            cargando = false;
        }
        cargarSalidas();
    }

    private void cargarSalidas() {
        if (cargando) {
            return;
        }
        cmbSalida.removeAllItems();
        int idPaquete = Formulario.idSeleccionado(cmbPaquete);
        if (idPaquete > 0) {
            try {
                cargando = true;
                for (SalidaDisponible s : servicio.salidas(idPaquete)) {
                    cmbSalida.addItem(s);
                }
            } catch (Exception e) {
                error(e);
            } finally {
                cargando = false;
            }
        }
        cargarPlanes();
    }

    private void cargarPlanes() {
        if (cargando) {
            return;
        }
        cmbPlan.removeAllItems();
        SalidaDisponible s = (SalidaDisponible) cmbSalida.getSelectedItem();
        if (s != null) {
            try {
                cargando = true;
                for (PlanVigente p : servicio.planes(Formulario.idSeleccionado(cmbPaquete), s.fecha())) {
                    cmbPlan.addItem(p);
                }
            } catch (Exception e) {
                error(e);
            } finally {
                cargando = false;
            }
        }
        refrescarTabla();
        actualizarTotal();
    }

    private void agregarme() {
        try {
            Optional<PasajeroNuevo> yo = servicio.yoComoPasajero();
            yo.ifPresent(this::agregar);
        } catch (Exception e) {
            error(e);
        }
    }

    private void agregar(PasajeroNuevo p) {
        for (PasajeroNuevo q : pasajeros) {
            if (q.tipoDocumento().equals(p.tipoDocumento()) && q.numeroDocumento().equals(p.numeroDocumento())) {
                info("Ese pasajero ya está en la lista.");
                return;
            }
        }
        pasajeros.add(p);
        refrescarTabla();
        actualizarTotal();
    }

    private void quitar() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            pasajeros.remove(fila);
            refrescarTabla();
            actualizarTotal();
        }
    }

    private void refrescarTabla() {
        modelo.setRowCount(0);
        SalidaDisponible s = (SalidaDisponible) cmbSalida.getSelectedItem();
        for (PasajeroNuevo p : pasajeros) {
            String edad = "";
            if (s != null) {
                int e = p.edadEn(s.fecha());
                edad = e + " años" + (e < 18 ? " (menor)" : "");
            }
            modelo.addRow(new Object[] {p.nombreCompleto(), p.tipoDocumento() + " " + p.numeroDocumento(),
                    Formato.fecha(p.fechaNacimiento()), edad});
        }
    }

    private void actualizarTotal() {
        PlanVigente plan = (PlanVigente) cmbPlan.getSelectedItem();
        if (plan == null) {
            lblPlan.setText(cmbSalida.getItemCount() == 0 ? "Este paquete no tiene salidas abiertas." : "No hay planes vigentes para esa fecha.");
            lblTotal.setText("Total: " + Formato.pesos(0));
            return;
        }
        lblPlan.setText("<html>" + plan.descripcion() + "</html>");
        lblTotal.setText("Total: " + Formato.pesos(plan.precio() * pasajeros.size())
                + "   (" + pasajeros.size() + " × " + Formato.pesos(plan.precio()) + ")");
    }

    private void reservar() {
        try {
            SalidaDisponible salida = (SalidaDisponible) cmbSalida.getSelectedItem();
            PlanVigente plan = (PlanVigente) cmbPlan.getSelectedItem();
            int idReserva = servicio.reservar(salida, plan, pasajeros);
            info("Reserva " + idReserva + " creada. Falta el pago.");
            PagoDialog.mostrar(this, idReserva);
            pasajeros.clear();
            cargarPaquetes();
            info("Reserva " + idReserva + " registrada. Puedes verla en «Mis reservas».");
        } catch (Exception e) {
            error(e);
            JOptionPane.showMessageDialog(this, ErroresBD.mensaje(e), "No se pudo reservar", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void info(String m) {
        lblEstado.setForeground(new Color(0x202124));
        lblEstado.setText(m);
    }

    private void error(Exception e) {
        lblEstado.setForeground(new Color(0xC62828));
        lblEstado.setText(ErroresBD.mensaje(e));
    }
}
