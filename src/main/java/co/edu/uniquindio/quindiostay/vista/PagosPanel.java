package co.edu.uniquindio.quindiostay.vista;

import javax.swing.JButton;
import java.util.List;
import java.util.Map;

import co.edu.uniquindio.quindiostay.logica.PagoService;
import co.edu.uniquindio.quindiostay.modelo.Pago;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;
import co.edu.uniquindio.quindiostay.vista.comun.Formato;

/**
 * CRUD de pagos para el personal. 
 * Nuevo registra un cobro; 
 * Editar cambia su estado (y la referencia en pagos en línea); 
 * Eliminar solo borra pagos pendientes o fallidos. 
 * El botón Factura abre el reporte de la reserva del pago seleccionado.
 */
public class PagosPanel extends CrudPanel<Pago> {
    private final PagoService servicio = new PagoService();

    public PagosPanel() {
        super("Pagos", "ID", "Fecha", "Reserva", "Monto", "Método", "Estado", "Referencia");
        JButton factura = new JButton("Factura");
        factura.addActionListener(e -> seleccionado().ifPresent(p -> VisorReporte.abrir(this, "factura",
                Map.of("ID_RESERVA", p.idReserva()), "Factura reserva " + p.idReserva())));
        agregarBoton(factura);
        cargar();
    }

    @Override
    protected List<Pago> consultar(String filtro) throws Exception {
        return servicio.consultar(filtro);
    }

    @Override
    protected Object[] fila(Pago p) {
        return new Object[] {
                p.id(), Formato.fechaHora(p.fechaPago()), p.reserva(), Formato.pesos(p.monto()),
                p.metodoPago(), p.estadoPago(), p.referencia() == null ? "" : p.referencia()
        };
    }

    @Override
    protected void nuevo() {
        if (PagoCrudDialog.mostrar(this, null)) {
            cargar();
            info("Pago registrado.");
        }
    }

    @Override
    protected void editar(Pago p) {
        if (PagoCrudDialog.mostrar(this, p)) {
            cargar();
            info("Pago actualizado.");
        }
    }

    @Override
    protected void eliminar(Pago p) throws Exception {
        servicio.eliminar(p);
    }

    @Override
    protected String describir(Pago p) {
        return "el pago " + p.id() + " de " + Formato.pesos(p.monto()) + " (reserva " + p.idReserva() + ")";
    }
}
