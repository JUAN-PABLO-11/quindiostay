package co.edu.uniquindio.quindiostay.logica;

import java.sql.SQLException;
import java.util.List;

import co.edu.uniquindio.quindiostay.datos.TipoComponenteDAO;
import co.edu.uniquindio.quindiostay.modelo.TipoComponente;

/**
 * valida antes de llamar al DAO. Solo el administrador puede modificar este catálogo. 
 * Oracle (UNIQUE, FK) es la segunda barrera.
 */
public class TipoComponenteService {
    private static final int MAX_NOMBRE = 15;

    private final TipoComponenteDAO dao = new TipoComponenteDAO();

    public List<TipoComponente> consultar(String filtro) throws SQLException {
        return dao.consultar(filtro);
    }

    /** Inserta si es nuevo (id == null) o actualiza si ya existe. */
    public void guardar(TipoComponente t) throws SQLException, ReglaException {
        exigirAdmin();
        validar(t);
        try {
            if (t.id() == null) {
                dao.insertar(t);
            } else {
                dao.actualizar(t);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {   // UNIQUE: nombre repetido
                throw new ReglaException("Ya existe el tipo de componente llamado \"" + t.nombre() + "\".");
            }
            throw e;
        }
    }

    public void eliminar(TipoComponente t) throws SQLException, ReglaException {
        exigirAdmin();
        try {
            dao.eliminar(t.id());
        } catch (SQLException e) {
            if (e.getErrorCode() == 2292) {   // RESTRICT: hay registros que lo usan
                throw new ReglaException("No se puede eliminar \"" + t.nombre()
                        + "\": hay registros que usan este tipo de componente (RESTRICT).");
            }
            throw e;
        }
    }

    private void validar(TipoComponente t) throws ReglaException {
        if (t.nombre() == null || t.nombre().isBlank()) {
            throw new ReglaException("El nombre es obligatorio.");
        }
        if (t.nombre().length() > MAX_NOMBRE) {
            throw new ReglaException("El nombre no puede tener más de " + MAX_NOMBRE + " caracteres.");
        }
    }

    private void exigirAdmin() throws ReglaException {
        if (!Sesion.esAdmin()) {
            throw new ReglaException("Solo el administrador puede modificar los tipos de componente.");
        }
    }
}
