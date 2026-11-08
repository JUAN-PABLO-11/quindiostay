package co.edu.uniquindio.quindiostay.logica;

import co.edu.uniquindio.quindiostay.datos.CuentaDAO;
import co.edu.uniquindio.quindiostay.datos.CuentaDAO.ClienteExistente;
import co.edu.uniquindio.quindiostay.modelo.RegistroCliente;
import co.edu.uniquindio.quindiostay.modelo.ReservaResumen;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Registro de clientes (decisión 1: la cuenta es opcional y vive en PERFIL_CLIENTE)
 * y consulta de "Mis reservas".
 */
public class CuentaService {

    public static final List<String> TIPOS_DOCUMENTO = List.of("CC", "CE", "pasaporte", "TI");
    public static final List<String> IDIOMAS = List.of("espanol", "ingles");
    public static final List<String> CANALES = List.of("correo", "sms", "whatsapp");
    public static final List<Integer> DIAS_ANTICIPACION = List.of(1, 3, 7);

    private final CuentaDAO dao = new CuentaDAO();

    /**
     * Crea la cuenta. Si el cliente ya existía sin cuenta (por ejemplo, compró en
     * mostrador) y el correo y el documento coinciden, solo se le crea el perfil.
     */
    public void registrar(RegistroCliente r, char[] clave, char[] confirmacion) throws SQLException, ReglaException {
        validar(r);
        validarClave(clave, confirmacion);
        String hash = LoginService.cifrar(clave);

        Optional<ClienteExistente> existente = dao.buscarPorCorreo(r.correo());
        if (existente.isPresent()) {
            ClienteExistente c = existente.get();
            if (c.tienePerfil()) {
                throw new ReglaException("Ya existe una cuenta con ese correo. Inicia sesión.");
            }
            if (!c.tipoDocumento().equals(r.tipoDocumento()) || !c.numeroDocumento().equals(r.numeroDocumento())) {
                throw new ReglaException("Ese correo ya está registrado con otro documento.");
            }
            dao.crearPerfil(c.id(), r, hash);
            return;
        }
        try {
            dao.registrar(r, hash);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {   // UNIQUE de documento
                throw new ReglaException("Ya hay un cliente con ese documento. Si ya compraste con nosotros, "
                        + "usa el mismo correo que diste en la agencia.");
            }
            throw e;
        }
    }

    /** Reservas del cliente que inició sesión. */
    public List<ReservaResumen> misReservas(String filtro) throws SQLException {
        return dao.reservasDe(Sesion.actual().id(), filtro);
    }

    private void validar(RegistroCliente r) throws ReglaException {
        texto(r.nombres(), "los nombres", 60);
        texto(r.apellidos(), "los apellidos", 60);
        if (!TIPOS_DOCUMENTO.contains(r.tipoDocumento())) {
            throw new ReglaException("Selecciona el tipo de documento.");
        }
        if (r.numeroDocumento() == null || !r.numeroDocumento().matches("[A-Za-z0-9]{5,15}")) {
            throw new ReglaException("El documento debe tener de 5 a 15 letras o números, sin puntos ni espacios.");
        }
        if (r.correo() == null || r.correo().length() > 100 || !r.correo().matches("[^@ ]+@[^@ ]+\\.[^@ ]+")) {
            throw new ReglaException("Escribe un correo válido, por ejemplo nombre@correo.com.");
        }
        if (r.telefono() == null || !r.telefono().matches("3\\d{9}")) {
            throw new ReglaException("El celular debe tener 10 dígitos y empezar por 3.");
        }
        if (r.fechaNacimiento() == null) {
            throw new ReglaException("Escribe la fecha de nacimiento.");
        }
        if (r.fechaNacimiento().isAfter(LocalDate.now()) || r.fechaNacimiento().isBefore(LocalDate.of(1900, 1, 1))) {
            throw new ReglaException("La fecha de nacimiento no es válida.");
        }
        texto(r.ciudadOrigen(), "la ciudad de origen", 60);
        if (!IDIOMAS.contains(r.idioma()) || !CANALES.contains(r.canalPreferido())
                || !DIAS_ANTICIPACION.contains(r.diasAnticipacion())) {
            throw new ReglaException("Revisa las preferencias de notificación.");
        }
    }

    private void validarClave(char[] clave, char[] confirmacion) throws ReglaException {
        String texto = new String(clave);
        if (texto.length() < 8 || !texto.matches(".*[A-Za-z].*") || !texto.matches(".*\\d.*")) {
            throw new ReglaException("La contraseña debe tener mínimo 8 caracteres, con letras y números.");
        }
        if (!Arrays.equals(clave, confirmacion)) {
            throw new ReglaException("Las contraseñas no coinciden.");
        }
    }

    private void texto(String valor, String campo, int maximo) throws ReglaException {
        if (valor == null || valor.isBlank()) {
            throw new ReglaException("Escribe " + campo + ".");
        }
        if (valor.length() > maximo) {
            throw new ReglaException("En " + campo + " caben máximo " + maximo + " caracteres.");
        }
    }
}
