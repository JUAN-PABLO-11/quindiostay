package co.edu.uniquindio.quindiostay.logica;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import co.edu.uniquindio.quindiostay.datos.ComponenteDAO;
import co.edu.uniquindio.quindiostay.datos.CotizacionDAO;
import co.edu.uniquindio.quindiostay.modelo.Componente;
import co.edu.uniquindio.quindiostay.modelo.Cotizacion;
import co.edu.uniquindio.quindiostay.modelo.DetalleCotizacion;

/**
 * Capa de lógica de las cotizaciones y su detalle.
 *
 * - El valor total es la suma de las líneas (nunca se escribe a mano) y debe ser mayor que 0,
 *   así que toda cotización lleva al menos una línea.
 * - RN25: la fecha de servicio de cada línea es posterior a la fecha de la cotización.
 * - La fecha de la cotización no puede ser futura. RN02 (prestador activo) y RN26 (una
 *   cotización de más de 8 días ya no se acepta) las revisa también Oracle.
 * - Una cotización aceptada ya no se modifica (puede haberse convertido en reserva).
 * - El encargado de prestador solo gestiona las cotizaciones de su prestador.
 */
public class CotizacionService {
    public static final List<String> ESTADOS = List.of("borrador", "enviada", "aceptada", "rechazada", "vencida");
    public static final int MAX_LINEAS = 50;
    public static final int VALOR_MAXIMO_LINEA = 2_000_000_000;

    private final CotizacionDAO dao = new CotizacionDAO();

    /** El encargado de prestador solo ve las cotizaciones que emite su prestador. */
    public List<Cotizacion> consultar(String filtro) throws SQLException, ReglaException {
        exigirAcceso();
        Integer soloPrestador = Sesion.esEncargado() ? Sesion.actual().idPrestador() : null;
        return dao.consultar(filtro, soloPrestador);
    }

    /** Las líneas de una cotización guardada. */
    public List<DetalleCotizacion> lineas(Cotizacion c) throws SQLException, ReglaException {
        exigirAcceso();
        verificarPrestador(c.idPrestador());
        return dao.lineas(c.id());
    }

    /** Componentes que se pueden cotizar (todos: una agencia puede cotizar componentes de otros prestadores). */
    public List<Componente> componentesDisponibles() throws SQLException, ReglaException {
        exigirAcceso();
        return new ComponenteDAO().consultar("", null);
    }

    /** Suma de los valores de las líneas. */
    public static long totalDe(List<DetalleCotizacion> lineas) {
        long total = 0;
        for (DetalleCotizacion d : lineas) {
            total += d.valor();
        }
        return total;
    }

    /** Valor sugerido de una línea: costo del componente x personas (el usuario puede ajustarlo). */
    public static long valorSugerido(Componente c, int personas) {
        return (long) c.costo() * personas;
    }

    /**
     * Valida una línea sola (lo usa el formulario de líneas para avisar antes de agregarla).
     *
     * @param fechaCotizacion fecha de la cotización a la que pertenece
     */
    public void validarLinea(DetalleCotizacion d, LocalDate fechaCotizacion) throws ReglaException {
        if (d.idComponente() <= 0) {
            throw new ReglaException("Selecciona el componente.");
        }
        if (d.fechaServicio() == null) {
            throw new ReglaException("La fecha de servicio es obligatoria.");
        }
        if (fechaCotizacion != null && !d.fechaServicio().isAfter(fechaCotizacion)) {
            throw new ReglaException("RN25: la fecha de servicio debe ser posterior a la fecha de la cotización ("
                    + fechaCotizacion + ").");
        }
        if (d.cantidadPersonas() < 1 || d.cantidadPersonas() > 20) {
            throw new ReglaException("La cantidad de personas debe estar entre 1 y 20.");
        }
        if (d.valor() <= 0 || d.valor() > VALOR_MAXIMO_LINEA) {
            throw new ReglaException("El valor de la línea debe ser mayor que 0 y no superar $2.000.000.000.");
        }
    }

    /**
     * Inserta si es nueva (id == null) o actualiza si ya existe, junto con todas sus líneas.
     * El número, el total y la numeración de las líneas los calcula esta clase.
     */
    public void guardar(Cotizacion c, List<DetalleCotizacion> lineas) throws SQLException, ReglaException {
        exigirAcceso();
        validar(c, lineas);
        verificarPrestador(c.idPrestador());

        if (c.id() != null) {
            Optional<Cotizacion> actual = dao.buscar(c.id());
            if (actual.isEmpty()) {
                throw new ReglaException("La cotización ya no existe.");
            }
            verificarPrestador(actual.get().idPrestador());
            if ("aceptada".equals(actual.get().estado())) {
                throw new ReglaException("La cotización " + actual.get().numero()
                        + " ya fue aceptada y no se puede modificar.");
            }
        }

        List<DetalleCotizacion> numeradas = renumerar(lineas);
        Cotizacion conTotal = new Cotizacion(c.id(), c.numero(), c.fecha(), totalDe(numeradas), c.estado(),
                c.idPrestador(), c.prestador(), c.idCliente(), c.cliente());
        try {
            if (c.id() == null) {
                dao.insertar(conTotal, numeradas);
            } else {
                dao.actualizar(conTotal, numeradas);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {   // UNIQUE del número: dos personas guardaron a la vez
                throw new ReglaException("Otra persona creó una cotización al mismo tiempo. Vuelve a guardar.");
            }
            throw e;
        }
    }

    public void eliminar(Cotizacion c) throws SQLException, ReglaException {
        exigirAcceso();
        verificarPrestador(c.idPrestador());
        try {
            dao.eliminar(c.id());   // el detalle se borra en cascada
        } catch (SQLException e) {
            if (e.getErrorCode() == 2292) {   // RESTRICT: ya se convirtió en reserva
                throw new ReglaException("No se puede eliminar la cotización " + c.numero()
                        + ": ya se convirtió en una reserva (RESTRICT).");
            }
            throw e;
        }
    }

    private void validar(Cotizacion c, List<DetalleCotizacion> lineas) throws ReglaException {
        if (c.fecha() == null) {
            throw new ReglaException("La fecha de la cotización es obligatoria.");
        }
        if (c.fecha().isAfter(LocalDate.now())) {
            throw new ReglaException("La fecha de la cotización no puede ser futura.");
        }
        if (!ESTADOS.contains(c.estado())) {
            throw new ReglaException("Selecciona el estado.");
        }
        if (c.idPrestador() <= 0) {
            throw new ReglaException("Selecciona el prestador que emite la cotización.");
        }
        if (c.idCliente() <= 0) {
            throw new ReglaException("Selecciona el cliente.");
        }
        if (lineas == null || lineas.isEmpty()) {
            throw new ReglaException("La cotización necesita al menos una línea (el total debe ser mayor que 0).");
        }
        if (lineas.size() > MAX_LINEAS) {
            throw new ReglaException("Una cotización no puede tener más de " + MAX_LINEAS + " líneas.");
        }
        for (DetalleCotizacion d : lineas) {
            validarLinea(d, c.fecha());
        }
    }

    /** Líneas numeradas 1..n en el orden recibido. */
    private List<DetalleCotizacion> renumerar(List<DetalleCotizacion> lineas) {
        List<DetalleCotizacion> resultado = new ArrayList<>();
        int n = 1;
        for (DetalleCotizacion d : lineas) {
            resultado.add(new DetalleCotizacion(n++, d.fechaServicio(), d.cantidadPersonas(), d.valor(),
                    d.idComponente(), d.componente()));
        }
        return resultado;
    }

    private void exigirAcceso() throws ReglaException {
        if (!Sesion.esAdmin() && !Sesion.esOperador() && !Sesion.esEncargado()) {
            throw new ReglaException("Tu rol no tiene acceso a la gestión de cotizaciones.");
        }
    }

    /** El encargado de prestador solo trabaja con las cotizaciones de su propio prestador. */
    private void verificarPrestador(int idPrestador) throws ReglaException {
        if (Sesion.esEncargado() && !Objects.equals(Sesion.actual().idPrestador(), idPrestador)) {
            throw new ReglaException("Solo puedes gestionar las cotizaciones de tu propio prestador.");
        }
    }
}
