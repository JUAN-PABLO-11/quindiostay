package co.edu.uniquindio.quindiostay.logica;

import co.edu.uniquindio.quindiostay.modelo.Municipio;
import co.edu.uniquindio.quindiostay.datos.MunicipioDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Mismas reglas de los CHECK (código DANE de 5 dígitos) y de los UNIQUE (código y nombre). 
 * Solo el administrador modifica.
 */
public class MunicipioService {
    private static final Pattern DANE = Pattern.compile("^[0-9]{5}$");

    private final MunicipioDAO dao = new MunicipioDAO();

    public List<Municipio> consultar(String filtro) throws SQLException {
        return dao.consultar(filtro);
    }

    /** Inserta si es nuevo (id == null) o actualiza si ya existe. */
    public void guardar(Municipio m) throws SQLException, ReglaException {
        exigirAdmin();
        validar(m);
        try {
            if (m.id() == null) {
                dao.insertar(m);
            } else {
                dao.actualizar(m);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {   // UNIQUE: código DANE o nombre repetidos
                throw new ReglaException("Ya existe un municipio con ese código DANE o con ese nombre.");
            }
            throw e;
        }
    }

    public void eliminar(Municipio m) throws SQLException, ReglaException {
        exigirAdmin();
        try {
            dao.eliminar(m.id());
        } catch (SQLException e) {
            if (e.getErrorCode() == 2292) {   // RESTRICT: tiene destinos o prestadores
                throw new ReglaException("No se puede eliminar \"" + m.nombre()
                        + "\": tiene destinos o prestadores registrados (RESTRICT).");
            }
            throw e;
        }
    }

    private void validar(Municipio m) throws ReglaException {
        if (m.codigoDane() == null || !DANE.matcher(m.codigoDane()).matches()) {
            throw new ReglaException("El código DANE debe tener exactamente 5 dígitos.");
        }
        if (m.nombre() == null || m.nombre().isBlank()) {
            throw new ReglaException("El nombre del municipio es obligatorio.");
        }
        if (m.nombre().length() > 30) {
            throw new ReglaException("El nombre no puede tener más de 30 caracteres.");
        }
    }

    private void exigirAdmin() throws ReglaException {
        if (!Sesion.esAdmin()) {
            throw new ReglaException("Solo el administrador puede modificar los municipios.");
        }
    }
}
