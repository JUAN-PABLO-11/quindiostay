package co.edu.uniquindio.quindiostay.vista;

import java.util.List;

import co.edu.uniquindio.quindiostay.logica.ComponenteService;
import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.modelo.Componente;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;
import co.edu.uniquindio.quindiostay.vista.comun.Formato;

/** CRUD de componentes. 
 *  Solo van las columnas y los botones. 
 */
public class ComponentesPanel extends CrudPanel<Componente>{
    private final ComponenteService servicio = new ComponenteService();

    public ComponentesPanel() {
        super("Componentes", "ID", "Nombre", "Tipo", "Prestador", "Costo");
        if (Sesion.esOperador()) {
            soloLectura();
        }
        cargar();
    }

    @Override
    protected List<Componente> consultar(String filtro) throws Exception {
        return servicio.consultar(filtro);
    }

    @Override
    protected Object[] fila(Componente c) {
        return new Object[] { c.id(), c.nombre(), c.tipoComponente(), c.prestador(), Formato.pesos(c.costo()) };
    }

    @Override
    protected void nuevo() {
        if (ComponenteDialog.mostrar(this, null)) {
            cargar();
            info("Componente creado.");
        }
    }

    @Override
    protected void editar(Componente c) {
        if (ComponenteDialog.mostrar(this, c)) {
            cargar();
            info("Componente actualizado.");
        }
    }

    @Override
    protected void eliminar(Componente c) throws Exception {
        servicio.eliminar(c);
    }

    @Override
    protected String describir(Componente c) {
        return "el componente \"" + c.nombre() + "\"";
    }
}
