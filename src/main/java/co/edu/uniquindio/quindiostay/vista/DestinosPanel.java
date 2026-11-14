package co.edu.uniquindio.quindiostay.vista;

import java.util.List;

import co.edu.uniquindio.quindiostay.logica.DestinoService;
import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.modelo.Destino;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;

/** 
 * CRUD de destinos. La pantalla la arma CrudPanel. 
 * Solo van las columnas y los botones. 
 */
public class DestinosPanel extends CrudPanel<Destino> {
    private final DestinoService servicio = new DestinoService();

    public DestinosPanel() {
        super("Destinos turísticos", "ID", "Nombre", "Municipio", "Descripción");
        if (!Sesion.esAdmin()) {
            soloLectura();
        }
        cargar();
    }

    @Override
    protected List<Destino> consultar(String filtro) throws Exception {
        return servicio.consultar(filtro);
    }

    @Override
    protected Object[] fila(Destino d) {
        return new Object[] { d.id(), d.nombre(), d.municipio(), d.descripcion() == null ? "" : d.descripcion() };
    }

    @Override
    protected void nuevo() {
        if (DestinoDialog.mostrar(this, null)) {
            cargar();
            info("Destino creado.");
        }
    }

    @Override
    protected void editar(Destino d) {
        if (DestinoDialog.mostrar(this, d)) {
            cargar();
            info("Destino actualizado.");
        }
    }

    @Override
    protected void eliminar(Destino d) throws Exception {
        servicio.eliminar(d);
    }

    @Override
    protected String describir(Destino d) {
        return "el destino \"" + d.nombre() + "\"";
    }
}
