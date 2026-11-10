package co.edu.uniquindio.quindiostay.vista;

import co.edu.uniquindio.quindiostay.logica.CuentaService;
import co.edu.uniquindio.quindiostay.modelo.ReservaResumen;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;
import co.edu.uniquindio.quindiostay.vista.comun.Formato;

import javax.swing.JButton;
import java.util.List;
import java.util.Map;

/**
 * "Mis reservas" del cliente: solo consulta (reutiliza CrudPanel sin botones).
 */
public class MisReservasPanel extends CrudPanel<ReservaResumen> {

    private final CuentaService servicio = new CuentaService();

    public MisReservasPanel() {
        super("Mis reservas",
                "Reserva", "Paquete", "Plan", "Salida", "Estado", "Pasajeros", "Valor total", "Pagado", "Saldo");
        soloLectura();
        boton("Pagar saldo", r -> {
            if (PagoDialog.mostrar(this, r.id())) {
                cargar();
            }
        });
        boton("Comprobante", r -> VisorReporte.abrir(this, "comprobante_reserva",
                Map.of("ID_RESERVA", r.id()), "Comprobante reserva " + r.id()));
        boton("Factura", r -> VisorReporte.abrir(this, "factura", Map.of("ID_RESERVA", r.id()), "Factura reserva " + r.id()));
        boton("Autorización de menores", r -> VisorReporte.abrir(this, "autorizacion_menores",
                Map.of("ID_RESERVA", r.id()), "Autorización de menores reserva " + r.id()));
        cargar();
    }

    /** Botón que actúa sobre la reserva seleccionada. */
    private void boton(String texto, java.util.function.Consumer<ReservaResumen> accion) {
        JButton b = new JButton(texto);
        b.addActionListener(e -> seleccionado().ifPresent(accion));
        agregarBoton(b);
    }

    @Override
    protected List<ReservaResumen> consultar(String filtro) throws Exception {
        return servicio.misReservas(filtro);
    }

    @Override
    protected Object[] fila(ReservaResumen r) {
        return new Object[] {
                r.id(), r.paquete(), r.plan(), Formato.fecha(r.fechaSalida()), r.estado(), r.pasajeros(),
                Formato.pesos(r.valorTotal()), Formato.pesos(r.pagado()), Formato.pesos(r.saldo())
        };
    }

    // Solo lectura: los botones están ocultos
    @Override
    protected void nuevo() {
    }

    @Override
    protected void editar(ReservaResumen r) {
    }

    @Override
    protected void eliminar(ReservaResumen r) {
    }

    @Override
    protected String describir(ReservaResumen r) {
        return "la reserva " + r.id();
    }
}
