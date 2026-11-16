package co.edu.uniquindio.quindiostay.vista;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import co.edu.uniquindio.quindiostay.logica.PasajeroService;
import co.edu.uniquindio.quindiostay.modelo.Pasajero;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;
import co.edu.uniquindio.quindiostay.vista.comun.Formato;

/** CRUD de pasajeros. La pantalla la arma CrudPanel.*/
public class PasajerosPanel extends CrudPanel<Pasajero> {
    private final PasajeroService servicio = new PasajeroService();

    public PasajerosPanel() {
        super("Pasajeros", "ID", "Nombre", "Documento", "Nacimiento", "Edad");
        cargar();
    }

    @Override
    protected List<Pasajero> consultar(String filtro) throws Exception {
        return servicio.consultar(filtro);
    }

    @Override
    protected Object[] fila(Pasajero p) {
        int edad = Period.between(p.fechaNacimiento(), LocalDate.now()).getYears();
        return new Object[] {
                p.id(), p.nombreCompleto(), p.tipoDocumento() + " " + p.numeroDocumento(),
                Formato.fecha(p.fechaNacimiento()), edad
        };
    }

    @Override
    protected void nuevo() {
        if (PasajeroCrudDialog.mostrar(this, null)) {
            cargar();
            info("Pasajero creado.");
        }
    }

    @Override
    protected void editar(Pasajero p) {
        if (PasajeroCrudDialog.mostrar(this, p)) {
            cargar();
            info("Pasajero actualizado.");
        }
    }

    @Override
    protected void eliminar(Pasajero p) throws Exception {
        servicio.eliminar(p);
    }

    @Override
    protected String describir(Pasajero p) {
        return "al pasajero \"" + p.nombreCompleto() + "\"";
    }
}
