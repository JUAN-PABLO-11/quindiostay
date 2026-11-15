package co.edu.uniquindio.quindiostay.logica;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import co.edu.uniquindio.quindiostay.datos.CatalogoDAO;
import co.edu.uniquindio.quindiostay.datos.SalidaDAO;
import co.edu.uniquindio.quindiostay.modelo.Salida;

/**
 * Una salida nueva nace "programada" y con fecha de hoy en adelante. 
 * RN16 (el cupo no baja de lo vendido) y RN17 (confirmar exige el mínimo de viajeros) 
 * las revisa también Oracle. 
 * El encargado de prestador solo gestiona salidas de sus paquetes; el operador solo consulta.
 */
public class SalidaService {
    public static final List<String> ESTADOS = List.of("programada", "confirmada", "cancelada", "realizada");

    private final SalidaDAO dao = new SalidaDAO();
    private final CatalogoDAO catalogo = new CatalogoDAO();

    /** El encargado de prestador solo ve las salidas de sus propios paquetes. */
    public List<Salida> consultar(String filtro) throws SQLException {
        Integer soloPrestador = Sesion.esEncargado() ? Sesion.actual().idPrestador() : null;
        return dao.consultar(filtro, soloPrestador);
    }

    /** Inserta si es nueva (id == null) o actualiza si ya existe. */
    public void guardar(Salida s) throws SQLException, ReglaException {
        validar(s);
        if (s.id() == null) {
            dao.insertar(s);
        } else {
            dao.actualizar(s);
        }
    }

    public void eliminar(Salida s) throws SQLException, ReglaException {
        verificarPermiso(s.idPaquete());
        try {
            dao.eliminar(s.id());
        } catch (SQLException e) {
            if (e.getErrorCode() == 2292) {   // RESTRICT: tiene reservas
                throw new ReglaException("No se puede eliminar la salida: tiene reservas registradas (RESTRICT). "
                        + "Si ya no se realizará, cámbiale el estado a cancelada.");
            }
            throw e;
        }
    }

    private void validar(Salida s) throws SQLException, ReglaException {
        if (s.idPaquete() <= 0) {
            throw new ReglaException("Selecciona el paquete.");
        }
        if (s.fechaInicio() == null) {
            throw new ReglaException("La fecha de inicio es obligatoria.");
        }
        if (s.cupoMaximo() < 1 || s.cupoMaximo() > 60) {
            throw new ReglaException("El cupo máximo debe estar entre 1 y 60 viajeros.");
        }
        if (!ESTADOS.contains(s.estado())) {
            throw new ReglaException("Selecciona el estado.");
        }
        if (s.id() == null) {
            if (!"programada".equals(s.estado())) {
                throw new ReglaException("Una salida nueva nace en estado programada.");
            }
            if (s.fechaInicio().isBefore(LocalDate.now())) {
                throw new ReglaException("La fecha de inicio de una salida nueva no puede ser anterior a hoy.");
            }
        } else if (s.cupoMaximo() < s.ocupados()) {
            throw new ReglaException("RN16: la salida ya tiene " + s.ocupados()
                    + " pasajeros vendidos; el cupo no puede ser menor.");
        }
        verificarPermiso(s.idPaquete());
    }

    /** El encargado solo gestiona salidas de sus paquetes; operador y cliente solo consultan. */
    private void verificarPermiso(int idPaquete) throws SQLException, ReglaException {
        if (Sesion.esOperador() || Sesion.esCliente()) {
            throw new ReglaException("Tu rol no tiene permiso para modificar salidas.");
        }
        if (Sesion.esEncargado()) {
            Integer dueno = catalogo.prestadorDePaquete(idPaquete);
            if (!Objects.equals(Sesion.actual().idPrestador(), dueno)) {
                throw new ReglaException("Solo puedes gestionar las salidas de los paquetes de tu propio prestador.");
            }
        }
    }
}
