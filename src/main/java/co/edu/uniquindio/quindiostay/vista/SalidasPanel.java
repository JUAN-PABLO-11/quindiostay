package co.edu.uniquindio.quindiostay.vista;

import java.util.List;

import co.edu.uniquindio.quindiostay.logica.SalidaService;
import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.modelo.Salida;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;
import co.edu.uniquindio.quindiostay.vista.comun.Formato;

/** CRUD de salidas. La pantalla la arma CrudPanel.*/
public class SalidasPanel extends CrudPanel<Salida> {
    private final SalidaService servicio = new SalidaService();

    public SalidasPanel() {
        super("Salidas", "ID", "Paquete", "Fecha de inicio", "Cupo (vendido / máximo)", "Estado");
        if (Sesion.esOperador()) {
            soloLectura();
        }
        cargar();
    }

    @Override
    protected List<Salida> consultar(String filtro) throws Exception {
        return servicio.consultar(filtro);
    }

    @Override
    protected Object[] fila(Salida s) {
        return new Object[] {
                s.id(), s.paquete(), Formato.fecha(s.fechaInicio()), s.ocupados() + " / " + s.cupoMaximo(), s.estado()
        };
    }

    @Override
    protected void nuevo() {
        if (SalidaDialog.mostrar(this, null)) {
            cargar();
            info("Salida creada.");
        }
    }

    @Override
    protected void editar(Salida s) {
        if (SalidaDialog.mostrar(this, s)) {
            cargar();
            info("Salida actualizada.");
        }
    }

    @Override
    protected void eliminar(Salida s) throws Exception {
        servicio.eliminar(s);
    }

    @Override
    protected String describir(Salida s) {
        return "la salida del " + Formato.fecha(s.fechaInicio()) + " de \"" + s.paquete() + "\"";
    }
}
