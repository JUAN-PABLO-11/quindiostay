package co.edu.uniquindio.quindiostay.logica;

/**
 * Error de validación de la capa de lógica (dato inválido o regla de negocio).
 * Su mensaje se muestra tal cual al usuario en la barra inferior.
 */
public class ReglaException extends Exception {
    public ReglaException(String mensaje) {
        super(mensaje);
    }
}
