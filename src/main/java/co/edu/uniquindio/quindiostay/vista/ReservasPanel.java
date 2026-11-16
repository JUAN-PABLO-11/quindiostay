package co.edu.uniquindio.quindiostay.vista;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import co.edu.uniquindio.quindiostay.logica.ReservaService;
import co.edu.uniquindio.quindiostay.modelo.PasajeroReserva;
import co.edu.uniquindio.quindiostay.modelo.Reserva;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;
import co.edu.uniquindio.quindiostay.vista.comun.Formato;

/**
 * CRUD de reservas para el personal. 
 * Nuevo crea la reserva con sus pasajeros; 
 * Editar cambia canal y estado; 
 * Eliminar no borra las canceladas (RN29). 
 * Botones propios: Cancelar reserva (procedimiento cancelar_reserva, RN31), Pasajeros, 
 * Comprobante y Factura (los reportes que ya existen).
 */
public class ReservasPanel extends CrudPanel<Reserva> {
    private final ReservaService servicio = new ReservaService();

    public ReservasPanel() {
        super("Reservas", "ID", "Fecha", "Cliente", "Paquete", "Plan", "Salida", "Pasajeros",
                "Valor total", "Pagado", "Saldo", "Estado", "Canal");
        boton("Cancelar reserva", this::cancelarReserva);
        boton("Pasajeros", this::verPasajeros);
        boton("Comprobante", r -> VisorReporte.abrir(this, "comprobante_reserva",
                Map.of("ID_RESERVA", r.id()), "Comprobante reserva " + r.id()));
        boton("Factura", r -> VisorReporte.abrir(this, "factura",
                Map.of("ID_RESERVA", r.id()), "Factura reserva " + r.id()));
        cargar();
    }

    /** Botón propio que actúa sobre la reserva seleccionada. */
    private void boton(String texto, Consumer<Reserva> accion) {
        JButton b = new JButton(texto);
        b.addActionListener(e -> seleccionado().ifPresent(accion));
        agregarBoton(b);
    }

    @Override
    protected List<Reserva> consultar(String filtro) throws Exception {
        return servicio.consultar(filtro);
    }

    @Override
    protected Object[] fila(Reserva r) {
        return new Object[] {
                r.id(), Formato.fecha(r.fechaCreacion()), r.cliente(), r.paquete(), r.plan(),
                Formato.fecha(r.fechaSalida()), r.pasajeros(), Formato.pesos(r.valorTotal()),
                Formato.pesos(r.pagado()), Formato.pesos(r.saldo()), r.estado(), r.canal()
        };
    }

    @Override
    protected void nuevo() {
        if (ReservaDialog.mostrar(this)) {
            cargar();
            info("Reserva creada.");
        }
    }

    @Override
    protected void editar(Reserva r) {
        if ("cancelada".equals(r.estado()) || "completada".equals(r.estado())) {
            info("La reserva " + r.id() + " está " + r.estado() + " y ya no se modifica.");
            return;
        }
        if (ReservaEdicionDialog.mostrar(this, r)) {
            cargar();
            info("Reserva actualizada.");
        }
    }

    @Override
    protected void eliminar(Reserva r) throws Exception {
        servicio.eliminar(r);
    }

    @Override
    protected String describir(Reserva r) {
        return "la reserva " + r.id() + " de " + r.cliente();
    }

    /** Pregunta quién cancela (de eso depende el reembolso) y llama al procedimiento cancelar_reserva. */
    private void cancelarReserva(Reserva r) {
        if ("cancelada".equals(r.estado()) || "completada".equals(r.estado())) {
            info("La reserva " + r.id() + " ya está " + r.estado() + ".");
            return;
        }
        String[] opciones = {"Cancela el cliente", "Cancela la agencia", "Volver"};
        int eleccion = JOptionPane.showOptionDialog(this,
                "Cancelar la reserva " + r.id() + " de " + r.cliente() + ".\n\n"
                        + "• Cancela el cliente: reembolso del 80 % de lo pagado si faltan más de 5 días para la salida; "
                        + "si no, sin reembolso.\n"
                        + "• Cancela la agencia: reembolso del 100 % de lo pagado.\n\n"
                        + "Lo pagado hasta hoy: " + Formato.pesos(r.pagado()),
                "Cancelar reserva", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, opciones, opciones[2]);
        if (eleccion != 0 && eleccion != 1) {
            return;
        }
        try {
            Reserva cancelada = servicio.cancelar(r, eleccion == 1);
            cargar();
            long reembolso = cancelada.valorReembolso() == null ? 0 : cancelada.valorReembolso();
            info("Reserva " + r.id() + " cancelada. Reembolso: " + Formato.pesos(reembolso) + ".");
        } catch (Exception e) {
            error(e);
        }
    }

    private void verPasajeros(Reserva r) {
        try {
            List<PasajeroReserva> lista = servicio.pasajerosDe(r);
            StringBuilder texto = new StringBuilder("Pasajeros de la reserva " + r.id() + ":\n\n");
            for (PasajeroReserva p : lista) {
                texto.append("• ").append(p.nombre()).append("  —  ").append(p.documento())
                        .append("  —  ").append(p.edadEnSalida()).append(" años").append(p.esMenor() ? " (menor)" : "")
                        .append('\n');
            }
            if (lista.isEmpty()) {
                texto.append("(sin pasajeros)");
            }
            JOptionPane.showMessageDialog(this, texto.toString(), "Pasajeros", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            error(e);
        }
    }
}
