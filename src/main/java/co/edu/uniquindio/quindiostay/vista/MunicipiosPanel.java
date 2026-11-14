package co.edu.uniquindio.quindiostay.vista;

import java.util.List;

import co.edu.uniquindio.quindiostay.logica.MunicipioService;
import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.modelo.Municipio;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;

public class MunicipiosPanel extends CrudPanel<Municipio>{
    private final MunicipioService servicio = new MunicipioService();

    public MunicipiosPanel() {
        super("Municipios", "ID", "Código DANE", "Nombre");
        if (!Sesion.esAdmin()) {
            soloLectura();
        }
        cargar();
    }

    @Override
    protected List<Municipio> consultar(String filtro) throws Exception {
        return servicio.consultar(filtro);
    }

    @Override
    protected Object[] fila(Municipio m) {
        return new Object[] { m.id(), m.codigoDane(), m.nombre() };
    }

    @Override
    protected void nuevo() {
        if (MunicipioDialog.mostrar(this, null)) {
            cargar();
            info("El municipio se creó.");
        }
    }

    @Override
    protected void editar(Municipio m) {
        if (MunicipioDialog.mostrar(this, m)) {
            cargar();
            info("El municipio se actualizó.");
        }
    }

    @Override
    protected void eliminar(Municipio m) throws Exception {
        servicio.eliminar(m);
    }

    @Override
    protected String describir(Municipio m) {
        return "el municipio \"" + m.nombre() + "\"";
    }
}
