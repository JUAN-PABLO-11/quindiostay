package co.edu.uniquindio.quindiostay.logica;

import java.sql.SQLException;
import java.util.List;

import co.edu.uniquindio.quindiostay.datos.TipoPrestadorDAO;
import co.edu.uniquindio.quindiostay.modelo.TipoPrestador;

/** 
 * valida antes de llamar al DAO. Solo el administrador puede modificar este catálogo. 
 * Oracle (UNIQUE, FK) es la segunda barrera.
*/
public class TipoPrestadorService {
    private static final int MAX_NOMBRE = 30;

    private final TipoPrestadorDAO dao = new TipoPrestadorDAO();

    public List<TipoPrestador> consultar(String filtro) throws SQLException {
        return dao.consultar(filtro);
    }

    /** Inserta si es nuevo (id == null) o actualiza si ya existe. */
    public void guardar(TipoPrestador t) throws SQLException, ReglaException {
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
                throw new ReglaException("Ya existe el tipo de prestador llamado \"" + t.nombre() + "\".");
            }
            throw e;
        }
    }

    public void eliminar(TipoPrestador t) throws SQLException, ReglaException {
        exigirAdmin();
        try {
            dao.eliminar(t.id());
        } catch (SQLException e) {
            if (e.getErrorCode() == 2292) {   // RESTRICT: hay registros que lo usan
                throw new ReglaException("No se puede eliminar \"" + t.nombre()
                        + "\": hay registros que usan este tipo de prestador (RESTRICT).");
            }
            throw e;
        }
    }

    private void validar(TipoPrestador t) throws ReglaException {
        if (t.nombre() == null || t.nombre().isBlank()) {
            throw new ReglaException("El nombre es obligatorio.");
        }
        if (t.nombre().length() > MAX_NOMBRE) {
            throw new ReglaException("El nombre no puede tener más de " + MAX_NOMBRE + " caracteres.");
        }
    }

    private void exigirAdmin() throws ReglaException {
        if (!Sesion.esAdmin()) {
            throw new ReglaException("Solo el administrador puede modificar los tipos de prestador.");
        }
    }
}
