package co.edu.uniquindio.quindiostay.vista;

import java.util.List;

import co.edu.uniquindio.quindiostay.logica.ItinerarioDiaService;
import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.modelo.ItinerarioDia;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;

/** CRUD del itinerario por día. */
public class ItinerarioDiaPanel extends CrudPanel<ItinerarioDia> {
    private final ItinerarioDiaService servicio = new ItinerarioDiaService();

    public ItinerarioDiaPanel() {
        super("Itinerario por día", "Paquete", "Día", "Descripción");
        if (Sesion.esOperador()) {
            soloLectura();
        }
        cargar();
    }

    @Override
    protected List<ItinerarioDia> consultar(String filtro) throws Exception {
        return servicio.consultar(filtro);
    }

    @Override
    protected Object[] fila(ItinerarioDia d) {
        return new Object[] { d.paquete(), d.numeroDia(), d.descripcion() };
    }

    @Override
    protected void nuevo() {
        if (ItinerarioDiaDialog.mostrar(this, null)) {
            cargar();
            info("Día agregado al itinerario.");
        }
    }

    @Override
    protected void editar(ItinerarioDia d) {
        if (ItinerarioDiaDialog.mostrar(this, d)) {
            cargar();
            info("Día actualizado.");
        }
    }

    @Override
    protected void eliminar(ItinerarioDia d) throws Exception {
        servicio.eliminar(d);
    }

    @Override
    protected String describir(ItinerarioDia d) {
        return "el día " + d.numeroDia() + " del paquete \"" + d.paquete() + "\"";
    }
}
