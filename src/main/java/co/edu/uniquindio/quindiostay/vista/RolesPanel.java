package co.edu.uniquindio.quindiostay.vista;

import java.util.List;

import co.edu.uniquindio.quindiostay.logica.RolService;
import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.modelo.Rol;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;

/** CRUD de los roles. La pantalla la arma CrudPanel. 
 * Solo van las columnas y los botones. 
 */
public class RolesPanel extends CrudPanel<Rol>{
    private final RolService servicio = new RolService();

    public RolesPanel() {
        super("Los roles", "ID", "Nombre");
        if (!Sesion.esAdmin()) {
            soloLectura();
        }
        cargar();
    }

    @Override
    protected List<Rol> consultar(String filtro) throws Exception {
        return servicio.consultar(filtro);
    }

    @Override
    protected Object[] fila(Rol rol) {
        return new Object[] { rol.id(), rol.nombre() };
    }

    @Override
    protected void nuevo() {
        if (RolDialog.mostrar(this, null)) {
            cargar();
            info("El rol se creó.");
        }
    }

    @Override
    protected void editar(Rol rol) {
        if (RolDialog.mostrar(this, rol)) {
            cargar();
            info("El rol se actualizó.");
        }
    }

    @Override
    protected void eliminar(Rol rol) throws Exception {
        servicio.eliminar(rol);
    }

    @Override
    protected String describir(Rol rol) {
        return "el rol \"" + rol.nombre() + "\"";
    }
}
