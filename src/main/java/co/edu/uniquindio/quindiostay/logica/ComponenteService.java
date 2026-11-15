package co.edu.uniquindio.quindiostay.logica;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import co.edu.uniquindio.quindiostay.datos.ComponenteDAO;
import co.edu.uniquindio.quindiostay.modelo.Componente;

/**
 * RN02 (solo prestadores activos) la revisa el trigger y su mensaje llega tal cual a la pantalla. 
 */
public class ComponenteService {
    private final ComponenteDAO dao = new ComponenteDAO();

    /** El encargado de prestador solo ve sus propios componentes. */
    public List<Componente> consultar(String filtro) throws SQLException {
        Integer soloPrestador = Sesion.esEncargado() ? Sesion.actual().idPrestador() : null;
        return dao.consultar(filtro, soloPrestador);
    }

    /** Inserta si es nuevo (id == null) o actualiza si ya existe. */
    public void guardar(Componente c) throws SQLException, ReglaException {
        validar(c);
        if (c.id() == null) {
            dao.insertar(c);
        } else {
            dao.actualizar(c);
        }
    }

    public void eliminar(Componente c) throws SQLException, ReglaException {
        verificarPermiso(c.idPrestador());
        try {
            dao.eliminar(c.id());
        } catch (SQLException e) {
            if (e.getErrorCode() == 2292) {   // RESTRICT: está en paquetes o cotizaciones
                throw new ReglaException("No se puede eliminar \"" + c.nombre()
                        + "\": está incluido en paquetes o en cotizaciones (RESTRICT). "
                        + "Quítalo primero de \"Componentes del paquete\".");
            }
            throw e;
        }
    }

    private void validar(Componente c) throws ReglaException {
        if (c.nombre() == null || c.nombre().isBlank()) {
            throw new ReglaException("El nombre del componente es obligatorio.");
        }
        if (c.nombre().length() > 100) {
            throw new ReglaException("El nombre no puede tener más de 100 caracteres.");
        }
        if (c.descripcion() != null && c.descripcion().length() > 500) {
            throw new ReglaException("La descripción no puede tener más de 500 caracteres.");
        }
        if (c.costo() <= 0) {
            throw new ReglaException("El costo debe ser mayor que 0.");
        }
        if (c.idTipoComponente() <= 0) {
            throw new ReglaException("Selecciona el tipo de componente.");
        }
        if (c.idPrestador() <= 0) {
            throw new ReglaException("Selecciona el prestador.");
        }
        verificarPermiso(c.idPrestador());
    }

    /** El encargado de prestador solo gestiona componentes de su propio prestador. */
    private void verificarPermiso(int idPrestador) throws ReglaException {
        if (Sesion.esEncargado() && !Objects.equals(Sesion.actual().idPrestador(), idPrestador)) {
            throw new ReglaException("Solo puedes gestionar los componentes de tu propio prestador.");
        }
        if (Sesion.esOperador() || Sesion.esCliente()) {
            throw new ReglaException("Tu rol no tiene permiso para modificar componentes.");
        }
    }
}
