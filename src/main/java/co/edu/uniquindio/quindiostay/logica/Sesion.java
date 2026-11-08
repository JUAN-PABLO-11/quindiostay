package co.edu.uniquindio.quindiostay.logica;

import co.edu.uniquindio.quindiostay.modelo.Usuario;

/**
 * Usuario que inició sesión. El rol decide qué opciones del menú se ven
 * (VentanaPrincipal) y qué datos se consultan (por ejemplo, el encargado de
 * prestador solo ve sus propios paquetes).
 */
public final class Sesion {

    public static final String ADMIN = "administrador de la plataforma";
    public static final String ENCARGADO = "encargado de prestador";
    public static final String OPERADOR = "operador";
    public static final String CLIENTE = "cliente";

    private static Usuario actual;

    private Sesion() {
    }

    public static void iniciar(Usuario usuario) {
        actual = usuario;
    }

    public static void cerrar() {
        actual = null;
    }

    public static Usuario actual() {
        return actual;
    }

    public static boolean esAdmin() {
        return tieneRol(ADMIN);
    }

    public static boolean esEncargado() {
        return tieneRol(ENCARGADO);
    }

    public static boolean esOperador() {
        return tieneRol(OPERADOR);
    }

    public static boolean esCliente() {
        return tieneRol(CLIENTE);
    }

    private static boolean tieneRol(String rol) {
        return actual != null && rol.equals(actual.rol());
    }
}
