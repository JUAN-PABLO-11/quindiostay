package co.edu.uniquindio.quindiostay.logica;

import co.edu.uniquindio.quindiostay.datos.PaqueteDAO;
import co.edu.uniquindio.quindiostay.modelo.Paquete;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * Capa de lógica de los paquetes: valida antes de llamar al DAO y decide
 * qué puede hacer cada rol.
 *
 * PLANTILLA: para otra tabla, copiar este archivo y cambiar validar().
 * Validar aquí da mensajes claros; Oracle (CHECK, FK, triggers) es la
 * segunda barrera por si algo se escapa.
 */
public class PaqueteService {

    public static final List<String> CATEGORIAS = List.of("cultural", "cafetero", "aventura", "naturaleza", "familiar");
    public static final List<String> VISIBILIDADES = List.of("publico", "privado");

    private final PaqueteDAO dao = new PaqueteDAO();

    /** El encargado de prestador solo ve sus propios paquetes. */
    public List<Paquete> consultar(String filtro) throws SQLException {
        Integer soloPrestador = Sesion.esEncargado() ? Sesion.actual().idPrestador() : null;
        return dao.consultar(filtro, soloPrestador);
    }

    /** Inserta si es nuevo (id == null) o actualiza si ya existe. */
    public void guardar(Paquete p) throws SQLException, ReglaException {
        validar(p);
        if (p.id() == null) {
            dao.insertar(p);
        } else {
            dao.actualizar(p);
        }
    }

    public void eliminar(Paquete p) throws SQLException, ReglaException {
        verificarPermiso(p.idPrestador());
        try {
            dao.eliminar(p.id());
        } catch (SQLException e) {
            if (e.getErrorCode() == 2292) {   // RESTRICT: tiene planes o salidas
                throw new ReglaException("No se puede eliminar \"" + p.nombre()
                        + "\": tiene planes o salidas registrados (RESTRICT). "
                        + "Si ya no se vende, cámbiale la visibilidad a privado.");
            }
            throw e;
        }
    }

    private void validar(Paquete p) throws ReglaException {
        if (p.nombre() == null || p.nombre().isBlank()) {
            throw new ReglaException("El nombre del paquete es obligatorio.");
        }
        if (p.nombre().length() > 100) {
            throw new ReglaException("El nombre no puede tener más de 100 caracteres.");
        }
        if (p.descripcion() == null || p.descripcion().isBlank()) {
            throw new ReglaException("La descripción es obligatoria.");
        }
        if (p.descripcion().length() > 1000) {
            throw new ReglaException("La descripción no puede tener más de 1000 caracteres.");
        }
        if (p.duracionDias() < 1 || p.duracionDias() > 30) {
            throw new ReglaException("La duración debe estar entre 1 y 30 días.");
        }
        if (p.minimoViajeros() < 1 || p.minimoViajeros() > 50) {
            throw new ReglaException("El mínimo de viajeros debe estar entre 1 y 50.");
        }
        if (!CATEGORIAS.contains(p.categoria())) {
            throw new ReglaException("Selecciona una categoría.");
        }
        if (!VISIBILIDADES.contains(p.visibilidad())) {
            throw new ReglaException("Selecciona la visibilidad.");
        }
        if (p.idDestino() <= 0) {
            throw new ReglaException("Selecciona el destino.");
        }
        if (p.idPrestador() <= 0) {
            throw new ReglaException("Selecciona el prestador.");
        }
        verificarPermiso(p.idPrestador());
    }

    /** El encargado de prestador solo gestiona paquetes de su propio prestador. */
    private void verificarPermiso(int idPrestador) throws ReglaException {
        if (Sesion.esEncargado() && !Objects.equals(Sesion.actual().idPrestador(), idPrestador)) {
            throw new ReglaException("Solo puedes gestionar los paquetes de tu propio prestador.");
        }
        if (Sesion.esOperador() || Sesion.esCliente()) {
            throw new ReglaException("Tu rol no tiene permiso para modificar paquetes.");
        }
    }
}
