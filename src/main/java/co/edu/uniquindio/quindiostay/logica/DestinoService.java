package co.edu.uniquindio.quindiostay.logica;

import java.sql.SQLException;
import java.util.List;

import co.edu.uniquindio.quindiostay.datos.DestinoDAO;
import co.edu.uniquindio.quindiostay.modelo.Destino;

public class DestinoService {
    private final DestinoDAO dao = new DestinoDAO();

    public List<Destino> consultar(String filtro) throws SQLException {
        return dao.consultar(filtro);
    }

    /** Inserta si es nuevo (id == null) o actualiza si ya existe. */
    public void guardar(Destino d) throws SQLException, ReglaException {
        exigirAdmin();
        validar(d);
        // La descripción es opcional: vacía se guarda como NULL
        if (d.descripcion() != null && d.descripcion().isBlank()) {
            d = new Destino(d.id(), d.nombre(), null, d.idMunicipio(), d.municipio());
        }
        if (d.id() == null) {
            dao.insertar(d);
        } else {
            dao.actualizar(d);
        }
    }

    public void eliminar(Destino d) throws SQLException, ReglaException {
        exigirAdmin();
        try {
            dao.eliminar(d.id());
        } catch (SQLException e) {
            if (e.getErrorCode() == 2292) {   // RESTRICT: tiene paquetes
                throw new ReglaException("No se puede eliminar \"" + d.nombre()
                        + "\": tiene paquetes turísticos registrados (RESTRICT).");
            }
            throw e;
        }
    }

    private void validar(Destino d) throws ReglaException {
        if (d.nombre() == null || d.nombre().isBlank()) {
            throw new ReglaException("El nombre del destino es obligatorio.");
        }
        if (d.nombre().length() > 80) {
            throw new ReglaException("El nombre no puede tener más de 80 caracteres.");
        }
        if (d.descripcion() != null && d.descripcion().length() > 500) {
            throw new ReglaException("La descripción no puede tener más de 500 caracteres.");
        }
        if (d.idMunicipio() <= 0) {
            throw new ReglaException("Selecciona el municipio.");
        }
    }

    private void exigirAdmin() throws ReglaException {
        if (!Sesion.esAdmin()) {
            throw new ReglaException("Solo el administrador puede modificar los destinos.");
        }
    }
}
