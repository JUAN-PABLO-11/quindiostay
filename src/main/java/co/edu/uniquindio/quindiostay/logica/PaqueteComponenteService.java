package co.edu.uniquindio.quindiostay.logica;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import co.edu.uniquindio.quindiostay.datos.CatalogoDAO;
import co.edu.uniquindio.quindiostay.datos.PaqueteComponenteDAO;
import co.edu.uniquindio.quindiostay.modelo.PaqueteComponente;

/**
 * Capa de lógica de los componentes de cada paquete y de la relación relación N:M. 
 */
public class PaqueteComponenteService {
    private final PaqueteComponenteDAO dao = new PaqueteComponenteDAO();
    private final CatalogoDAO catalogo = new CatalogoDAO();

    /** El encargado de prestador solo ve los componentes de sus propios paquetes. */
    public List<PaqueteComponente> consultar(String filtro) throws SQLException {
        Integer soloPrestador = Sesion.esEncargado() ? Sesion.actual().idPrestador() : null;
        return dao.consultar(filtro, soloPrestador);
    }

    /** Componentes que ya tiene el paquete. */
    public Set<Integer> componentesDe(int idPaquete) throws SQLException {
        return dao.idsDelPaquete(idPaquete);
    }

    /** Guarda el conjunto completo de componentes del paquete
     * Lo desmarcado se quita, lo marcado se agrega. 
     */
    public void asignar(int idPaquete, Set<Integer> idsComponentes) throws SQLException, ReglaException {
        if (idPaquete <= 0) {
            throw new ReglaException("Selecciona el paquete.");
        }
        verificarPermiso(idPaquete);
        dao.reemplazar(idPaquete, new HashSet<>(idsComponentes));
    }

    public void eliminar(PaqueteComponente pc) throws SQLException, ReglaException {
        verificarPermiso(pc.idPaquete());
        dao.eliminar(pc.idPaquete(), pc.idComponente());
    }

    /** El encargado solo gestiona lo de sus propios paquetes; operador y cliente solo consultan. */
    private void verificarPermiso(int idPaquete) throws SQLException, ReglaException {
        if (Sesion.esOperador() || Sesion.esCliente()) {
            throw new ReglaException("Tu rol no tiene permiso para modificar los componentes de un paquete.");
        }
        if (Sesion.esEncargado()) {
            Integer dueno = catalogo.prestadorDePaquete(idPaquete);
            if (!Objects.equals(Sesion.actual().idPrestador(), dueno)) {
                throw new ReglaException("Solo puedes gestionar los componentes de los paquetes de tu propio prestador.");
            }
        }
    }
}
