package co.edu.uniquindio.quindiostay.vista;

import co.edu.uniquindio.quindiostay.logica.PaqueteService;
import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.modelo.Paquete;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;

import java.util.List;

/**
 * CRUD de paquetes. Toda la pantalla (buscador, botones, tabla, barra de
 * mensajes) la arma CrudPanel; aquí solo se dice qué columnas mostrar y
 * qué hacer en cada botón.
 *
 * PLANTILLA: para otra tabla, copiar este archivo y cambiar las columnas,
 * fila() y el tipo (Paquete -> Prestador, PaqueteService -> PrestadorService...).
 */
public class PaquetesPanel extends CrudPanel<Paquete> {

    private final PaqueteService servicio = new PaqueteService();

    public PaquetesPanel() {
        super("Paquetes turísticos",
                "ID", "Nombre", "Destino", "Categoría", "Días", "Mín. viajeros", "Visibilidad", "Prestador");
        if (Sesion.esOperador()) {
            soloLectura();
        }
        cargar();
    }

    @Override
    protected List<Paquete> consultar(String filtro) throws Exception {
        return servicio.consultar(filtro);
    }

    @Override
    protected Object[] fila(Paquete p) {
        return new Object[] {
                p.id(), p.nombre(), p.destino(), p.categoria(),
                p.duracionDias(), p.minimoViajeros(), p.visibilidad(), p.prestador()
        };
    }

    @Override
    protected void nuevo() {
        if (PaqueteDialog.mostrar(this, null)) {
            cargar();
            info("Paquete creado.");
        }
    }

    @Override
    protected void editar(Paquete p) {
        if (PaqueteDialog.mostrar(this, p)) {
            cargar();
            info("Paquete actualizado.");
        }
    }

    @Override
    protected void eliminar(Paquete p) throws Exception {
        servicio.eliminar(p);
    }

    @Override
    protected String describir(Paquete p) {
        return "el paquete \"" + p.nombre() + "\"";
    }
}
