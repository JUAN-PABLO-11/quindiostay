package co.edu.uniquindio.quindiostay.vista;

import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;

import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.logica.NotificacionGestionService;
import co.edu.uniquindio.quindiostay.modelo.NotificacionGestion;
import co.edu.uniquindio.quindiostay.modelo.Opcion;
import co.edu.uniquindio.quindiostay.vista.comun.Formulario;
import co.edu.uniquindio.quindiostay.vista.comun.SelectorFecha;

/**
 * Formulario para crear o editar una notificación. 
 * La fecha programada se elige con el selector de fecha más la hora y los minutos. 
 * Al cambiar el cliente, el combo de reservas muestra solo las de él.
 * El mensaje es multilínea y se avisa cuánto falta para el máximo permitido por la base de datos.
 */
public class NotificacionGestionDialog extends JDialog {
    private final NotificacionGestionService servicio = new NotificacionGestionService();
    private final NotificacionGestion original;   // null = nueva
    private boolean guardado = false;
    private boolean cargando = false;

    private final JComboBox<Opcion> cmbCliente = new JComboBox<>();
    private final JComboBox<String> cmbTipo =
            new JComboBox<>(NotificacionGestionService.TIPOS.toArray(String[]::new));
    private final JComboBox<String> cmbCanal =
            new JComboBox<>(NotificacionGestionService.CANALES.toArray(String[]::new));
    private final JComboBox<String> cmbEstadoNotif =
            new JComboBox<>(NotificacionGestionService.ESTADOS.toArray(String[]::new));
    private final JComboBox<Opcion> cmbReserva = new JComboBox<>();
    private final SelectorFecha selFecha = new SelectorFecha(LocalDate.now().getYear() - 2, LocalDate.now().getYear() + 2);
    private final JSpinner spnHora = new JSpinner(new SpinnerNumberModel(8, 0, 23, 1));
    private final JSpinner spnMinuto = new JSpinner(new SpinnerNumberModel(0, 0, 59, 5));
    private final JTextArea txtMensaje;
    private final JLabel lblContador = new JLabel(" ");
    private final JLabel lblEstado = new JLabel(" ");

    /** Abre el formulario y devuelve true si se guardó. */
    public static boolean mostrar(Component padre, NotificacionGestion notificacion) {
        NotificacionGestionDialog dialogo =
                new NotificacionGestionDialog(SwingUtilities.getWindowAncestor(padre), notificacion);
        dialogo.setVisible(true);   // modal: se queda aquí hasta que se cierre
        return dialogo.guardado;
    }

    private NotificacionGestionDialog(Window padre, NotificacionGestion notificacion) {
        super(padre, notificacion == null ? "Nueva notificación" : "Editar notificación", ModalityType.APPLICATION_MODAL);
        this.original = notificacion;

        JPanel hora = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hora.add(spnHora);
        hora.add(new JLabel(":"));
        hora.add(spnMinuto);

        Formulario f = new Formulario();
        f.campo("Cliente", cmbCliente);
        f.campo("Reserva (opcional)", cmbReserva);
        f.campo("Tipo", cmbTipo);
        f.campo("Canal", cmbCanal);
        f.campo("Fecha programada", selFecha);
        f.campo("Hora programada", hora);
        f.campo("Estado", cmbEstadoNotif);
        txtMensaje = f.areaTexto("Mensaje", 4);

        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnCancelar);
        botones.add(btnGuardar);

        lblContador.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        lblEstado.setForeground(new Color(0xC62828));
        lblEstado.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        JPanel mensajes = new JPanel(new BorderLayout());
        mensajes.add(lblContador, BorderLayout.NORTH);
        mensajes.add(lblEstado, BorderLayout.CENTER);
        JPanel sur = new JPanel(new BorderLayout());
        sur.add(mensajes, BorderLayout.CENTER);
        sur.add(botones, BorderLayout.SOUTH);

        add(f, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnGuardar);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        txtMensaje.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                actualizarContador();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                actualizarContador();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                actualizarContador();
            }
        });

        cargarClientes();
        cmbCliente.addActionListener(e -> cargarReservas(0));
        if (notificacion != null) {
            llenar(notificacion);
        } else {
            cmbCliente.setSelectedIndex(-1);
            cmbTipo.setSelectedIndex(-1);
            cmbCanal.setSelectedItem("correo");
            cmbEstadoNotif.setSelectedItem("pendiente");
            selFecha.setFecha(LocalDate.now());
        }
        actualizarContador();
        pack();
        setLocationRelativeTo(padre);
    }

    private void cargarClientes() {
        try {
            Formulario.llenar(cmbCliente, servicio.clientes());
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        }
    }

    /** Muestra solo las reservas del cliente elegido, con "(ninguna)" como primera opción. */
    private void cargarReservas(int idSeleccionar) {
        if (cargando) {
            return;
        }
        cargando = true;
        try {
            List<Opcion> opciones = new ArrayList<>();
            opciones.add(new Opcion(0, "(ninguna)"));
            int idCliente = Formulario.idSeleccionado(cmbCliente);
            if (idCliente > 0) {
                opciones.addAll(servicio.reservasDeCliente(idCliente));
            }
            Formulario.llenar(cmbReserva, opciones);
            Formulario.seleccionar(cmbReserva, idSeleccionar, "(ninguna)");
        } catch (Exception e) {
            lblEstado.setText(ErroresBD.mensaje(e));
        } finally {
            cargando = false;
        }
    }

    private void llenar(NotificacionGestion n) {
        Formulario.seleccionar(cmbCliente, n.idCliente(), n.cliente());
        cargarReservas(n.idReserva() == null ? 0 : n.idReserva());
        cmbTipo.setSelectedItem(n.tipo());
        cmbCanal.setSelectedItem(n.canal());
        cmbEstadoNotif.setSelectedItem(n.estado());
        selFecha.setFecha(n.fechaProgramada().toLocalDate());
        spnHora.setValue(n.fechaProgramada().getHour());
        spnMinuto.setValue(n.fechaProgramada().getMinute());
        txtMensaje.setText(n.mensaje());
    }

    private void actualizarContador() {
        int usados = txtMensaje.getText().length();
        int max = NotificacionGestionService.MAX_MENSAJE;
        lblContador.setText(usados + " / " + max + " caracteres");
        lblContador.setForeground(usados > max ? new Color(0xC62828) : Color.GRAY);
    }

    private NotificacionGestion construir() {
        int idReserva = Formulario.idSeleccionado(cmbReserva);
        LocalDateTime programada = LocalDateTime.of(selFecha.getFecha(),
                LocalTime.of((Integer) spnHora.getValue(), (Integer) spnMinuto.getValue()));
        return new NotificacionGestion(
                original == null ? null : original.id(),
                (String) cmbTipo.getSelectedItem(),
                (String) cmbCanal.getSelectedItem(),
                txtMensaje.getText().trim(),
                programada,
                original == null ? null : original.fechaEnvio(),
                (String) cmbEstadoNotif.getSelectedItem(),
                Formulario.idSeleccionado(cmbCliente),
                Formulario.textoSeleccionado(cmbCliente),
                idReserva > 0 ? idReserva : null);
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
