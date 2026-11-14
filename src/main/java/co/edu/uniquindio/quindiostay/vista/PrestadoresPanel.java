package co.edu.uniquindio.quindiostay.vista;

import java.util.List;

import co.edu.uniquindio.quindiostay.logica.PrestadorService;
import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.modelo.Prestador;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;

/** CRUD de prestadores. La pantalla la arma CrudPanel. 
 * Solo van las columnas y los botones. 
 */
public class PrestadoresPanel extends CrudPanel<Prestador> {
    private final PrestadorService servicio = new PrestadorService();

    public PrestadoresPanel() {
        super("Prestadores turísticos",
                "ID", "Nombre comercial", "NIT", "RNT", "Tipo", "Municipio", "Teléfono", "Correo", "Estado");
        if (!Sesion.esAdmin()) {
            soloLectura();
        }
        cargar();
    }

    @Override
    protected List<Prestador> consultar(String filtro) throws Exception {
        return servicio.consultar(filtro);
    }

    @Override
    protected Object[] fila(Prestador p) {
        return new Object[] {
                p.id(), p.nombreComercial(), p.nit(), p.numeroRnt(), p.tipoPrestador(),
                p.municipio(), p.telefono(), p.correo(), p.estado()
        };
    }

    @Override
    protected void nuevo() {
        if (PrestadorDialog.mostrar(this, null)) {
            cargar();
            info("Prestador creado.");
        }
    }

    @Override
    protected void editar(Prestador p) {
        if (PrestadorDialog.mostrar(this, p)) {
            cargar();
            info("Prestador actualizado.");
        }
    }

    @Override
    protected void eliminar(Prestador p) throws Exception {
        servicio.eliminar(p);
    }

    @Override
    protected String describir(Prestador p) {
        return "el prestador \"" + p.nombreComercial() + "\"";
    }
}
