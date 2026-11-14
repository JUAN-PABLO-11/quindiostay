package co.edu.uniquindio.quindiostay.logica;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import co.edu.uniquindio.quindiostay.datos.RolDAO;
import co.edu.uniquindio.quindiostay.modelo.Rol;

/**
 * Valida antes de llamar al DAO. Solo el administrador puede modificar este catálogo. 
 * Oracle (UNIQUE, FK) es la segunda barrera.
 */
public class RolService {
    private static final int MAX_NOMBRE = 30;

    /** Roles que el programa usa para decidir permisos (Sesion): no se pueden renombrar ni borrar. */
    private static final Set<String> DEL_SISTEMA = Set.of(Sesion.ADMIN, Sesion.ENCARGADO, Sesion.OPERADOR);

    private final RolDAO dao = new RolDAO();

    public List<Rol> consultar(String filtro) throws SQLException {
        return dao.consultar(filtro);
    }

    /** Inserta si es nuevo (id == null) o actualiza si ya existe. */
    public void guardar(Rol r) throws SQLException, ReglaException {
        exigirAdmin();
        validar(r);
        if (r.id() != null) {
            String actual = dao.nombrePorId(r.id());
            if (actual != null && DEL_SISTEMA.contains(actual) && !actual.equals(r.nombre())) {
                throw new ReglaException("El rol \"" + actual + "\" lo usa el sistema para los permisos: no se puede renombrar.");
            }
        }

        try {
            if (r.id() == null) {
                dao.insertar(r);
            } else {
                dao.actualizar(r);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {   // UNIQUE: nombre repetido
                throw new ReglaException("Ya existe el rol llamado \"" + r.nombre() + "\".");
            }
            throw e;
        }
    }

    public void eliminar(Rol r) throws SQLException, ReglaException {
        exigirAdmin();
        if (DEL_SISTEMA.contains(r.nombre())) {
            throw new ReglaException("El rol \"" + r.nombre() + "\" lo usa el sistema para los permisos: no se puede eliminar.");
        }

        try {
            dao.eliminar(r.id());
        } catch (SQLException e) {
            if (e.getErrorCode() == 2292) {   // RESTRICT: hay registros que lo usan
                throw new ReglaException("No se puede eliminar \"" + r.nombre()
                        + "\": hay registros que usan este rol (RESTRICT).");
            }
            throw e;
        }
    }

    private void validar(Rol r) throws ReglaException {
        if (r.nombre() == null || r.nombre().isBlank()) {
            throw new ReglaException("El nombre es obligatorio.");
        }
        if (r.nombre().length() > MAX_NOMBRE) {
            throw new ReglaException("El nombre no puede tener más de " + MAX_NOMBRE + " caracteres.");
        }
    }

    private void exigirAdmin() throws ReglaException {
        if (!Sesion.esAdmin()) {
            throw new ReglaException("Solo el administrador puede modificar los roles.");
        }
    }
}
