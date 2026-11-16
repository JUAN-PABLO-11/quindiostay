package co.edu.uniquindio.quindiostay.logica;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

import co.edu.uniquindio.quindiostay.datos.PasajeroDAO;
import co.edu.uniquindio.quindiostay.modelo.Pasajero;

/**
 * Repite las reglas de los CHECK (documento) y del trigger (nacimiento no futuro). 
 * RN30 (un menor no viaja sin adulto) se valida al armar la reserva, no aquí. 
 * Solo el administrador y el operador gestionan pasajeros.
 */
public class PasajeroService {
    public static final List<String> TIPOS_DOCUMENTO = List.of("RC", "TI", "CC", "CE", "pasaporte");

    private static final Pattern DOCUMENTO = Pattern.compile("^[A-Za-z0-9]{5,15}$");
    private static final LocalDate NACIMIENTO_MINIMO = LocalDate.of(1900, 1, 1);

    private final PasajeroDAO dao = new PasajeroDAO();

    public List<Pasajero> consultar(String filtro) throws SQLException, ReglaException {
        exigirAcceso();
        return dao.consultar(filtro);
    }

    /** Inserta si es nuevo (id == null) o actualiza si ya existe. */
    public void guardar(Pasajero p) throws SQLException, ReglaException {
        exigirAcceso();
        validar(p);
        try {
            if (p.id() == null) {
                dao.insertar(p);
            } else {
                dao.actualizar(p);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {   // UNIQUE: documento repetido
                throw new ReglaException("Ya existe un pasajero con ese tipo y número de documento.");
            }
            throw e;
        }
    }

    public void eliminar(Pasajero p) throws SQLException, ReglaException {
        exigirAcceso();
        try {
            dao.eliminar(p.id());
        } catch (SQLException e) {
            if (e.getErrorCode() == 2292) {   // RESTRICT: viaja en alguna reserva
                throw new ReglaException("No se puede eliminar a " + p.nombreCompleto()
                        + ": viaja en reservas registradas (RESTRICT).");
            }
            throw e;
        }
    }

    private void validar(Pasajero p) throws ReglaException {
        texto(p.nombres(), "nombres");
        texto(p.apellidos(), "apellidos");
        if (!TIPOS_DOCUMENTO.contains(p.tipoDocumento())) {
            throw new ReglaException("Selecciona el tipo de documento.");
        }
        if (p.numeroDocumento() == null || !DOCUMENTO.matcher(p.numeroDocumento()).matches()) {
            throw new ReglaException("El número de documento debe tener entre 5 y 15 letras o dígitos, sin espacios.");
        }
        if (p.fechaNacimiento() == null || p.fechaNacimiento().isBefore(NACIMIENTO_MINIMO)) {
            throw new ReglaException("La fecha de nacimiento no puede ser anterior a 1900.");
        }
        if (p.fechaNacimiento().isAfter(LocalDate.now())) {
            throw new ReglaException("La fecha de nacimiento del pasajero no puede ser futura.");
        }
    }

    private void texto(String valor, String campo) throws ReglaException {
        if (valor == null || valor.isBlank()) {
            throw new ReglaException("El campo " + campo + " es obligatorio.");
        }
        if (valor.length() > 60) {
            throw new ReglaException("El campo " + campo + " no puede tener más de 60 caracteres.");
        }
    }

    private void exigirAcceso() throws ReglaException {
        if (!Sesion.esAdmin() && !Sesion.esOperador()) {
            throw new ReglaException("Tu rol no tiene acceso a la gestión de pasajeros.");
        }
    }
}
