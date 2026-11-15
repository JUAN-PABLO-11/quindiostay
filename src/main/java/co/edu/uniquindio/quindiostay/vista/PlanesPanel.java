package co.edu.uniquindio.quindiostay.vista;

import java.util.List;

import co.edu.uniquindio.quindiostay.logica.PlanService;
import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.modelo.Plan;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;
import co.edu.uniquindio.quindiostay.vista.comun.Formato;

/** CRUD de planes. */
public class PlanesPanel extends CrudPanel<Plan> {
    private final PlanService servicio = new PlanService();

    public PlanesPanel() {
        super("Planes", "ID", "Paquete", "Plan", "Tipo", "Precio", "Desde", "Hasta", "Estado");
        if (Sesion.esOperador()) {
            soloLectura();
        }
        cargar();
    }

    @Override
    protected List<Plan> consultar(String filtro) throws Exception {
        return servicio.consultar(filtro);
    }

    @Override
    protected Object[] fila(Plan p) {
        return new Object[] {
                p.id(), p.paquete(), p.nombre(), p.tipoPlan(), Formato.pesos(p.precio()),
                Formato.fecha(p.fechaInicio()), Formato.fecha(p.fechaFin()), p.estado()
        };
    }

    @Override
    protected void nuevo() {
        if (PlanDialog.mostrar(this, null)) {
            cargar();
            info("Plan creado.");
        }
    }

    @Override
    protected void editar(Plan p) {
        if (PlanDialog.mostrar(this, p)) {
            cargar();
            info("Plan actualizado.");
        }
    }

    @Override
    protected void eliminar(Plan p) throws Exception {
        servicio.eliminar(p);
    }

    @Override
    protected String describir(Plan p) {
        return "el plan \"" + p.nombre() + "\" de \"" + p.paquete() + "\"";
    }
}
