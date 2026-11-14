package co.edu.uniquindio.quindiostay.vista;

import java.util.List;

import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.logica.TipoComponenteService;
import co.edu.uniquindio.quindiostay.modelo.TipoComponente;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;

public class TiposComponentePanel extends CrudPanel<TipoComponente>{
    private final TipoComponenteService servicio = new TipoComponenteService();

    public TiposComponentePanel() {
        super("Los tipos de componente", "ID", "Nombre");
        if (!Sesion.esAdmin()) {
            soloLectura();
        }
        cargar();
    }

    @Override
    protected List<TipoComponente> consultar(String filtro) throws Exception {
        return servicio.consultar(filtro);
    }

    @Override
    protected Object[] fila(TipoComponente tipoComponente) {
        return new Object[] { tipoComponente.id(), tipoComponente.nombre() };
    }

    @Override
    protected void nuevo() {
        if (TipoComponenteDialog.mostrar(this, null)) {
            cargar();
            info("El tipo de componente se creó.");
        }
    }

    @Override
    protected void editar(TipoComponente tipoComponente) {
        if (TipoComponenteDialog.mostrar(this, tipoComponente)) {
            cargar();
            info("El tipo de componente se actualizó.");
        }
    }

    @Override
    protected void eliminar(TipoComponente tipoComponente) throws Exception {
        servicio.eliminar(tipoComponente);
    }

    @Override
    protected String describir(TipoComponente tipoComponente) {
        return "el tipo de componente \"" + tipoComponente.nombre() + "\"";
    }
}
