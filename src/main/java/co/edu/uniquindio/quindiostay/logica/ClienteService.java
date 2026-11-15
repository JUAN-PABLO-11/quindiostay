package co.edu.uniquindio.quindiostay.logica;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

import co.edu.uniquindio.quindiostay.datos.ClienteDAO;
import co.edu.uniquindio.quindiostay.modelo.Cliente;

/**
 * Repite las reglas de los CHECK (documento, correo, celular) y 
 * la del trigger (nacimiento no futuro). Solo el administrador y 
 * el operador gestionan clientes: el encargado de prestador no ve datos personales.
 */
public class ClienteService {
    public static final List<String> TIPOS_DOCUMENTO = List.of("CC", "CE", "pasaporte", "TI");

    private static final Pattern DOCUMENTO = Pattern.compile("^[A-Za-z0-9]{5,15}$");
    private static final Pattern CORREO = Pattern.compile("^[^@ ]+@[^@ ]+\\.[^@ ]+$");
    private static final Pattern CELULAR = Pattern.compile("^3[0-9]{9}$");
    private static final LocalDate NACIMIENTO_MINIMO = LocalDate.of(1900, 1, 1);

    private final ClienteDAO dao = new ClienteDAO();

    public List<Cliente> consultar(String filtro) throws SQLException, ReglaException {
        exigirAcceso();
        return dao.consultar(filtro);
    }

    /** Inserta si es nuevo (id == null) o actualiza si ya existe. */
    public void guardar(Cliente c) throws SQLException, ReglaException {
        exigirAcceso();
        validar(c);
        try {
            if (c.id() == null) {
                dao.insertar(c);
            } else {
                dao.actualizar(c);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {   // UNIQUE: correo o documento repetidos
                String texto = e.getMessage() == null ? "" : e.getMessage().toUpperCase();
                throw new ReglaException(texto.contains("CORREO")
                        ? "Ya existe un cliente con ese correo."
                        : "Ya existe un cliente con ese tipo y número de documento.");
            }
            throw e;
        }
    }

    public void eliminar(Cliente c) throws SQLException, ReglaException {
        exigirAcceso();
        try {
            dao.eliminar(c.id());
        } catch (SQLException e) {
            if (e.getErrorCode() == 2292) {   // RESTRICT: tiene cotizaciones o reservas
                throw new ReglaException("No se puede eliminar a " + c.nombreCompleto()
                        + ": tiene cotizaciones o reservas registradas (RESTRICT).");
            }
            throw e;
        }
    }

    private void validar(Cliente c) throws ReglaException {
        texto(c.nombres(), "nombres", 60);
        texto(c.apellidos(), "apellidos", 60);
        if (!TIPOS_DOCUMENTO.contains(c.tipoDocumento())) {
            throw new ReglaException("Selecciona el tipo de documento.");
        }
        if (c.numeroDocumento() == null || !DOCUMENTO.matcher(c.numeroDocumento()).matches()) {
            throw new ReglaException("El número de documento debe tener entre 5 y 15 letras o dígitos, sin espacios.");
        }
        if (c.correo() == null || c.correo().length() > 100 || !CORREO.matcher(c.correo()).matches()) {
            throw new ReglaException("El correo no es válido (máximo 100 caracteres).");
        }
        if (c.telefono() == null || !CELULAR.matcher(c.telefono()).matches()) {
            throw new ReglaException("El teléfono debe ser un celular de 10 dígitos que empiece por 3.");
        }
        if (c.fechaNacimiento() == null || c.fechaNacimiento().isBefore(NACIMIENTO_MINIMO)) {
            throw new ReglaException("La fecha de nacimiento no puede ser anterior a 1900.");
        }
        if (c.fechaNacimiento().isAfter(LocalDate.now())) {
            throw new ReglaException("La fecha de nacimiento del cliente no puede ser futura.");
        }
        texto(c.ciudadOrigen(), "ciudad de origen", 60);
    }

    private void texto(String valor, String campo, int max) throws ReglaException {
        if (valor == null || valor.isBlank()) {
            throw new ReglaException("El campo " + campo + " es obligatorio.");
        }
        if (valor.length() > max) {
            throw new ReglaException("El campo " + campo + " no puede tener más de " + max + " caracteres.");
        }
    }

    private void exigirAcceso() throws ReglaException {
        if (!Sesion.esAdmin() && !Sesion.esOperador()) {
            throw new ReglaException("Tu rol no tiene acceso a la gestión de clientes.");
        }
    }
}
