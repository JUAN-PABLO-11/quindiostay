package co.edu.uniquindio.quindiostay.logica;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Traduce cualquier error a un mensaje para la barra inferior de la ventana.
 *
 *  - ReglaException: se muestra tal cual (validaciones de la capa de lógica).
 *  - ORA-20xxx: errores de los triggers (RAISE_APPLICATION_ERROR); se muestra
 *    el texto del trigger, que ya dice qué regla (RNxx) falló.
 *  - ORA-00001, 02290, 02291, 02292...: restricciones de Oracle; se muestra
 *    el nombre de la restricción (uq_, ck_, fk_...), que dice qué campo falló.
 */
public final class ErroresBD {

    private static final Pattern RESTRICCION = Pattern.compile("\\(\\w+\\.(\\w+)\\)");

    private ErroresBD() {
    }

    public static String mensaje(Throwable e) {
        // JasperReports y otras librerías envuelven el error real: se busca en la cadena de causas
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof ReglaException) {
                return t.getMessage();
            }
            if (t instanceof SQLException sql) {
                return mensajeSql(sql);
            }
        }
        if (e.getClass().getName().startsWith("net.sf.jasperreports")) {
            return "No se pudo generar el documento: " + e.getMessage();
        }
        return "Error inesperado: " + e.getMessage();
    }

    private static String mensajeSql(SQLException e) {
        int codigo = e.getErrorCode();
        String texto = e.getMessage() == null ? "" : e.getMessage();

        if (codigo >= 20000 && codigo <= 20999) {
            return primeraLinea(texto).replaceFirst("^ORA-\\d{5}:\\s*", "");
        }
        return switch (codigo) {
            case 1 -> "Ya existe un registro con ese dato (" + restriccion(texto) + ").";
            case 1400 -> "Falta un dato obligatorio.";
            case 2290 -> "Un dato no cumple las reglas de la base de datos (" + restriccion(texto) + ").";
            case 2291 -> "El registro relacionado no existe (" + restriccion(texto) + ").";
            case 2292 -> "No se puede eliminar: hay registros que dependen de él (" + restriccion(texto) + ").";
            case 12899 -> "Un texto supera la longitud permitida.";
            case 1017 -> "Usuario o clave de Oracle incorrectos: revisa db.properties.";
            case 17002, 12514, 12541, 12505 ->
                    "No se pudo conectar a Oracle. Revisa que Docker esté corriendo (docker compose up -d).";
            default -> primeraLinea(texto);
        };
    }

    /** Nombre de la restricción en minúscula, por ejemplo "ck_paquete_duracion". */
    private static String restriccion(String texto) {
        Matcher m = RESTRICCION.matcher(texto);
        return m.find() ? m.group(1).toLowerCase() : "?";
    }

    private static String primeraLinea(String texto) {
        int salto = texto.indexOf('\n');
        return (salto >= 0 ? texto.substring(0, salto) : texto).trim();
    }
}
