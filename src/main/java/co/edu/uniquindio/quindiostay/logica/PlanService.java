package co.edu.uniquindio.quindiostay.logica;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

import co.edu.uniquindio.quindiostay.datos.CatalogoDAO;
import co.edu.uniquindio.quindiostay.datos.PlanDAO;
import co.edu.uniquindio.quindiostay.modelo.Plan;

/**
 * Repite las reglas de los CHECK (tipo, precio, vigencia, estado) 
 * y del UNIQUE de RN09 (un paquete no repite el nombre de un plan).
 */
public class PlanService {
    public static final List<String> TIPOS = List.of("basico", "estandar", "premium", "todo incluido");
    public static final List<String> ESTADOS = List.of("activo", "inactivo");
    public static final int PRECIO_MAXIMO = 50_000_000;

    private final PlanDAO dao = new PlanDAO();
    private final CatalogoDAO catalogo = new CatalogoDAO();

    /** El encargado de prestador solo ve los planes de sus propios paquetes. */
    public List<Plan> consultar(String filtro) throws SQLException {
        Integer soloPrestador = Sesion.esEncargado() ? Sesion.actual().idPrestador() : null;
        return dao.consultar(filtro, soloPrestador);
    }

    /** Convierte el texto aaaa-mm-dd del formulario en fecha. */
    public static LocalDate parsearFecha(String campo, String texto) throws ReglaException {
        try {
            return LocalDate.parse(texto == null ? "" : texto.trim());
        } catch (DateTimeParseException e) {
            throw new ReglaException(campo + " no es válida: usa el formato aaaa-mm-dd (por ejemplo 2026-12-31).");
        }
    }

    /** Inserta si es nuevo (id == null) o actualiza si ya existe. */
    public void guardar(Plan p) throws SQLException, ReglaException {
        validar(p);
        try {
            if (p.id() == null) {
                dao.insertar(p);
            } else {
                dao.actualizar(p);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {   // RN09: UNIQUE (paquete, nombre)
                throw new ReglaException("RN09: el paquete \"" + p.paquete()
                        + "\" ya tiene un plan llamado \"" + p.nombre() + "\".");
            }
            throw e;
        }
    }

    public void eliminar(Plan p) throws SQLException, ReglaException {
        verificarPermiso(p.idPaquete());
        try {
            dao.eliminar(p.id());
        } catch (SQLException e) {
            if (e.getErrorCode() == 2292) {   // RESTRICT: tiene reservas
                throw new ReglaException("No se puede eliminar \"" + p.nombre()
                        + "\": tiene reservas registradas (RESTRICT). Si ya no se vende, ponlo en estado inactivo.");
            }
            throw e;
        }
    }

    private void validar(Plan p) throws SQLException, ReglaException {
        if (p.nombre() == null || p.nombre().isBlank()) {
            throw new ReglaException("El nombre del plan es obligatorio.");
        }
        if (p.nombre().length() > 80) {
            throw new ReglaException("El nombre no puede tener más de 80 caracteres.");
        }
        if (p.descripcion() == null || p.descripcion().isBlank()) {
            throw new ReglaException("La descripción es obligatoria.");
        }
        if (p.descripcion().length() > 1000) {
            throw new ReglaException("La descripción no puede tener más de 1000 caracteres.");
        }
        if (!TIPOS.contains(p.tipoPlan())) {
            throw new ReglaException("Selecciona el tipo de plan.");
        }
        if (p.precio() <= 0 || p.precio() > PRECIO_MAXIMO) {
            throw new ReglaException("El precio debe ser mayor que 0 y no superar $50.000.000.");
        }
        if (p.fechaInicio() == null || p.fechaFin() == null) {
            throw new ReglaException("Las fechas de vigencia son obligatorias.");
        }
        if (p.fechaFin().isBefore(p.fechaInicio())) {
            throw new ReglaException("La fecha fin de vigencia no puede ser anterior a la de inicio.");
        }
        if (!ESTADOS.contains(p.estado())) {
            throw new ReglaException("Selecciona el estado.");
        }
        if (p.idPaquete() <= 0) {
            throw new ReglaException("Selecciona el paquete.");
        }
        verificarPermiso(p.idPaquete());
    }

    /** El encargado solo gestiona lo de sus propios paquetes; operador y cliente solo consultan. */
    private void verificarPermiso(int idPaquete) throws SQLException, ReglaException {
        if (Sesion.esOperador() || Sesion.esCliente()) {
            throw new ReglaException("Tu rol no tiene permiso para modificar planes.");
        }
        if (Sesion.esEncargado()) {
            Integer dueno = catalogo.prestadorDePaquete(idPaquete);
            if (!Objects.equals(Sesion.actual().idPrestador(), dueno)) {
                throw new ReglaException("Solo puedes gestionar los planes de los paquetes de tu propio prestador.");
            }
        }
    }
}
