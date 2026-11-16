package co.edu.uniquindio.quindiostay.vista;

import java.util.List;

import co.edu.uniquindio.quindiostay.logica.CotizacionService;
import co.edu.uniquindio.quindiostay.modelo.Cotizacion;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;
import co.edu.uniquindio.quindiostay.vista.comun.Formato;

public class CotizacionesPanel extends CrudPanel<Cotizacion>{
    private final CotizacionService servicio = new CotizacionService();

    public CotizacionesPanel() {
        super("Cotizaciones", "ID", "Número", "Fecha", "Cliente", "Prestador", "Valor total", "Estado");
        cargar();
    }

    @Override
    protected List<Cotizacion> consultar(String filtro) throws Exception {
        return servicio.consultar(filtro);
    }

    @Override
    protected Object[] fila(Cotizacion c) {
        return new Object[] {
                c.id(), c.numero(), Formato.fecha(c.fecha()), c.cliente(), c.prestador(),
                Formato.pesos(c.valorTotal()), c.estado()
        };
    }

    @Override
    protected void nuevo() {
        if (CotizacionDialog.mostrar(this, null)) {
            cargar();
            info("Cotización creada.");
        }
    }

    @Override
    protected void editar(Cotizacion c) {
        if ("aceptada".equals(c.estado())) {
            info("La cotización " + c.numero() + " ya fue aceptada y no se puede modificar.");
            return;
        }
        if (CotizacionDialog.mostrar(this, c)) {
            cargar();
            info("Cotización actualizada.");
        }
    }

    @Override
    protected void eliminar(Cotizacion c) throws Exception {
        servicio.eliminar(c);
    }

    @Override
    protected String describir(Cotizacion c) {
        return "la cotización " + c.numero() + " con todas sus líneas";
    }
}
