package co.edu.uniquindio.quindiostay.vista;

import java.util.List;

import co.edu.uniquindio.quindiostay.logica.PaqueteComponenteService;
import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.modelo.PaqueteComponente;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;

/**
 * CRUD de los componentes de cada paquete (tabla puente). 
 * Nuevo abre el formulario de casillas para cualquier paquete; 
 * Editar lo abre ya posicionado en el paquete de la fila;
 * Eliminar quita un solo componente de ese paquete.
 */
public class ComponentesPaquetePanel extends CrudPanel<PaqueteComponente> {
    private final PaqueteComponenteService servicio = new PaqueteComponenteService();

    public ComponentesPaquetePanel() {
        super("Componentes del paquete", "Paquete", "Tipo", "Componente");
        btnNuevo.setText("Asignar componentes");
        btnEditar.setText("Editar paquete");
        if (Sesion.esOperador()) {
            soloLectura();
        }
        cargar();
    }

    @Override
    protected List<PaqueteComponente> consultar(String filtro) throws Exception {
        return servicio.consultar(filtro);
    }

    @Override
    protected Object[] fila(PaqueteComponente pc) {
        return new Object[] { pc.paquete(), pc.tipoComponente(), pc.componente() };
    }

    @Override
    protected void nuevo() {
        if (ComponentesPaqueteDialog.mostrar(this, null, null)) {
            cargar();
            info("Componentes del paquete guardados.");
        }
    }

    @Override
    protected void editar(PaqueteComponente pc) {
        if (ComponentesPaqueteDialog.mostrar(this, pc.idPaquete(), pc.paquete())) {
            cargar();
            info("Componentes del paquete \"" + pc.paquete() + "\" actualizados.");
        }
    }

    @Override
    protected void eliminar(PaqueteComponente pc) throws Exception {
        servicio.eliminar(pc);
    }

    @Override
    protected String describir(PaqueteComponente pc) {
        return "el componente \"" + pc.componente() + "\" del paquete \"" + pc.paquete() + "\"";
    }
}
