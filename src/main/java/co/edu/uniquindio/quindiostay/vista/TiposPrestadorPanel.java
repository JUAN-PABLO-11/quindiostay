package co.edu.uniquindio.quindiostay.vista;

import java.util.List;

import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.logica.TipoPrestadorService;
import co.edu.uniquindio.quindiostay.modelo.TipoPrestador;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;

public class TiposPrestadorPanel extends CrudPanel<TipoPrestador> {
    private final TipoPrestadorService servicio = new TipoPrestadorService();

    public TiposPrestadorPanel() {
        super("Los tipos de prestador", "ID", "Nombre");
        if (!Sesion.esAdmin()) {
            soloLectura();
        }
        cargar();
    }

    @Override
    protected List<TipoPrestador> consultar(String filtro) throws Exception {
        return servicio.consultar(filtro);
    }

    @Override
    protected Object[] fila(TipoPrestador tipoPrestador) {
        return new Object[] { tipoPrestador.id(), tipoPrestador.nombre() };
    }

    @Override
    protected void nuevo() {
        if (TipoPrestadorDialog.mostrar(this, null)) {
            cargar();
            info("El tipo de prestador se creó.");
        }
    }

    @Override
    protected void editar(TipoPrestador tipoPrestador) {
        if (TipoPrestadorDialog.mostrar(this, tipoPrestador)) {
            cargar();
            info("El tipo de prestador se actualizó.");
        }
    }

    @Override
    protected void eliminar(TipoPrestador tipoPrestador) throws Exception {
        servicio.eliminar(tipoPrestador);
    }

    @Override
    protected String describir(TipoPrestador tipoPrestador) {
        return "el tipo de prestador \"" + tipoPrestador.nombre() + "\"";
    }
}
