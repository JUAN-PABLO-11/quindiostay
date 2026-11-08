package co.edu.uniquindio.quindiostay.vista;

import co.edu.uniquindio.quindiostay.logica.CuentaService;
import co.edu.uniquindio.quindiostay.modelo.ReservaResumen;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;
import co.edu.uniquindio.quindiostay.vista.comun.Formato;

import java.util.List;

/**
 * "Mis reservas" del cliente: solo consulta (reutiliza CrudPanel sin botones).
 */
public class MisReservasPanel extends CrudPanel<ReservaResumen> {

    private final CuentaService servicio = new CuentaService();

    public MisReservasPanel() {
        super("Mis reservas",
                "Reserva", "Paquete", "Plan", "Salida", "Estado", "Pasajeros", "Valor total", "Pagado", "Saldo");
        soloLectura();
        cargar();
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
