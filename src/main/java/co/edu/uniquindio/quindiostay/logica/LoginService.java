package co.edu.uniquindio.quindiostay.logica;

import co.edu.uniquindio.quindiostay.datos.UsuarioDAO;
import co.edu.uniquindio.quindiostay.datos.UsuarioDAO.Credencial;
import co.edu.uniquindio.quindiostay.modelo.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Inicio de sesión único para usuarios internos y clientes:
 * si lo escrito tiene "@" se busca como correo de cliente; si no, como usuario interno.
 * La contraseña escrita se compara con el hash guardado usando bcrypt.
 */
public class LoginService {

    private final UsuarioDAO dao = new UsuarioDAO();

    public Usuario iniciarSesion(String usuario, char[] clave) throws SQLException, ReglaException {
        if (usuario == null || usuario.isBlank() || clave == null || clave.length == 0) {
            throw new ReglaException("Escribe tu usuario (o correo) y tu contraseña.");
        }
        String texto = usuario.trim();
        Optional<Credencial> credencial = texto.contains("@")
                ? dao.buscarCliente(texto)
                : dao.buscarInterno(texto);

        // Mismo mensaje si no existe o si la clave no coincide: no revela qué usuarios existen
        if (credencial.isEmpty() || !BCrypt.checkpw(new String(clave), credencial.get().hash())) {
            throw new ReglaException("Usuario o contraseña incorrectos.");
        }
        if (!"activo".equals(credencial.get().estado())) {
            throw new ReglaException("Tu usuario está bloqueado. Habla con el administrador.");
        }
        Sesion.iniciar(credencial.get().usuario());
        return credencial.get().usuario();
    }

    /** Para crear usuarios o cuentas: cifra la contraseña antes de guardarla. */
    public static String cifrar(char[] clave) {
        return BCrypt.hashpw(new String(clave), BCrypt.gensalt(10));
    }
}
