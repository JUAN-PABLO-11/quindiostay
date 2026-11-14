package co.edu.uniquindio.quindiostay.logica;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import co.edu.uniquindio.quindiostay.datos.UsuarioSistemaDAO;
import co.edu.uniquindio.quindiostay.modelo.UsuarioSistema;

/**
 * Reglas: nombre de usuario de 4 a 30 caracteres sin espacios (CHECK), contraseña
 * de mínimo 8 caracteres que se cifra con bcrypt (LoginService.cifrar), RN03 (el
 * encargado de prestador lleva prestador; los demás roles no) y que el
 * administrador no pueda bloquearse, quitarse el rol ni borrarse a sí mismo.
 */
public class UsuarioSistemaService {
    public static final List<String> ESTADOS = List.of("activo", "bloqueado");

    private static final Pattern NOMBRE = Pattern.compile("^[^ ]{4,30}$");
    private static final int CLAVE_MIN = 8;
    private static final int CLAVE_MAX = 72;   // bcrypt solo usa los primeros 72 bytes

    private final UsuarioSistemaDAO dao = new UsuarioSistemaDAO();

    public List<UsuarioSistema> consultar(String filtro) throws SQLException {
        return dao.consultar(filtro);
    }

    /**
     * Inserta si es nuevo (id == null) o actualiza si ya existe.
     *
     * @param clave contraseña en claro. Obligatoria al crear; al editar, si viene
     *              vacía o null se conserva la actual. Esta clase no la modifica:
     *              quien la entrega debe borrarla (Arrays.fill) cuando termine.
     */
    public void guardar(UsuarioSistema u, char[] clave) throws SQLException, ReglaException {
        exigirAdmin();
        validar(u);
        boolean hayClave = clave != null && clave.length > 0;
        if (u.id() == null && !hayClave) {
            throw new ReglaException("La contraseña es obligatoria para un usuario nuevo.");
        }
        if (hayClave) {
            validarClave(clave);
        }
        verificarAutoproteccion(u);

        String hash = hayClave ? LoginService.cifrar(clave) : null;
        try {
            if (u.id() == null) {
                dao.insertar(u, hash);
            } else {
                dao.actualizar(u, hash);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {   // UNIQUE: nombre de usuario repetido
                throw new ReglaException("Ya existe un usuario llamado \"" + u.nombreUsuario() + "\".");
            }
            throw e;
        }
    }

    public void eliminar(UsuarioSistema u) throws SQLException, ReglaException {
        exigirAdmin();
        if (esElMismo(u)) {
            throw new ReglaException("No puedes eliminar tu propio usuario mientras tienes la sesión abierta.");
        }
        dao.eliminar(u.id());
    }

    private void validar(UsuarioSistema u) throws ReglaException {
        if (u.nombreUsuario() == null || !NOMBRE.matcher(u.nombreUsuario()).matches()) {
            throw new ReglaException("El nombre de usuario debe tener entre 4 y 30 caracteres y no puede llevar espacios.");
        }
        if (!ESTADOS.contains(u.estado())) {
            throw new ReglaException("Selecciona el estado.");
        }
        if (u.idRol() <= 0) {
            throw new ReglaException("Selecciona el rol.");
        }
        // RN03 (el trigger también la revisa; aquí el mensaje sale antes y más claro)
        boolean encargado = Sesion.ENCARGADO.equals(u.rol());
        if (encargado && u.idPrestador() == null) {
            throw new ReglaException("RN03: un encargado de prestador debe estar ligado a un prestador.");
        }
        if (!encargado && u.idPrestador() != null) {
            throw new ReglaException("RN03: solo el rol \"" + Sesion.ENCARGADO + "\" puede estar ligado a un prestador.");
        }
    }

    private void validarClave(char[] clave) throws ReglaException {
        if (clave.length < CLAVE_MIN) {
            throw new ReglaException("La contraseña debe tener mínimo " + CLAVE_MIN + " caracteres.");
        }
        if (clave.length > CLAVE_MAX) {
            throw new ReglaException("La contraseña no puede tener más de " + CLAVE_MAX + " caracteres.");
        }
    }

    /** Evita que el administrador se deje sin acceso por accidente. */
    private void verificarAutoproteccion(UsuarioSistema u) throws ReglaException {
        if (u.id() == null || !esElMismo(u)) {
            return;
        }
        if (!"activo".equals(u.estado())) {
            throw new ReglaException("No puedes bloquear tu propio usuario.");
        }
        if (!Sesion.ADMIN.equals(u.rol())) {
            throw new ReglaException("No puedes quitarte a ti mismo el rol de administrador.");
        }
    }

    private boolean esElMismo(UsuarioSistema u) {
        return Sesion.actual() != null && !Sesion.esCliente() && Objects.equals(Sesion.actual().id(), u.id());
    }

    private void exigirAdmin() throws ReglaException {
        if (!Sesion.esAdmin()) {
            throw new ReglaException("Solo el administrador puede gestionar los usuarios del sistema.");
        }
    }
}
