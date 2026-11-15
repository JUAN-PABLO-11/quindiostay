package co.edu.uniquindio.quindiostay.logica;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import co.edu.uniquindio.quindiostay.datos.CatalogoDAO;
import co.edu.uniquindio.quindiostay.datos.ItinerarioDiaDAO;
import co.edu.uniquindio.quindiostay.modelo.ItinerarioDia;

/**
 * La PK es compuesta, por lo que el formulario dice si es nuevo o edición (guardar(dia, esNuevo)). 
 * RN11 (el día no supera la duración del paquete) la revisa el trigger y su mensaje llega a la pantalla.
 */
public class ItinerarioDiaService {
    private final ItinerarioDiaDAO dao = new ItinerarioDiaDAO();
    private final CatalogoDAO catalogo = new CatalogoDAO();

    /** El encargado de prestador solo ve los itinerarios de sus propios paquetes. */
    public List<ItinerarioDia> consultar(String filtro) throws SQLException {
        Integer soloPrestador = Sesion.esEncargado() ? Sesion.actual().idPrestador() : null;
        return dao.consultar(filtro, soloPrestador);
    }

    public void guardar(ItinerarioDia d, boolean esNuevo) throws SQLException, ReglaException {
        validar(d);
        try {
            if (esNuevo) {
                dao.insertar(d);
            } else {
                dao.actualizar(d);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {   // PK repetida: ese día ya existe
                throw new ReglaException("El paquete \"" + d.paquete() + "\" ya tiene el día " + d.numeroDia()
                        + " en su itinerario. Edítalo en vez de crearlo otra vez.");
            }
            throw e;
        }
    }

    public void eliminar(ItinerarioDia d) throws SQLException, ReglaException {
        verificarPermiso(d.idPaquete());
        dao.eliminar(d.idPaquete(), d.numeroDia());
    }

    private void validar(ItinerarioDia d) throws SQLException, ReglaException {
        if (d.idPaquete() <= 0) {
            throw new ReglaException("Selecciona el paquete.");
        }
        if (d.numeroDia() < 1 || d.numeroDia() > 30) {
            throw new ReglaException("El número de día debe estar entre 1 y 30.");
        }
        if (d.descripcion() == null || d.descripcion().isBlank()) {
            throw new ReglaException("La descripción del día es obligatoria.");
        }
        if (d.descripcion().length() > 500) {
            throw new ReglaException("La descripción no puede tener más de 500 caracteres.");
        }
        verificarPermiso(d.idPaquete());
    }

    /** El encargado solo gestiona lo de sus propios paquetes; operador y cliente solo consultan. */
    private void verificarPermiso(int idPaquete) throws SQLException, ReglaException {
        if (Sesion.esOperador() || Sesion.esCliente()) {
            throw new ReglaException("Tu rol no tiene permiso para modificar itinerarios.");
        }
        if (Sesion.esEncargado()) {
            Integer dueno = catalogo.prestadorDePaquete(idPaquete);
            if (!Objects.equals(Sesion.actual().idPrestador(), dueno)) {
                throw new ReglaException("Solo puedes gestionar el itinerario de los paquetes de tu propio prestador.");
            }
        }
    }
}
