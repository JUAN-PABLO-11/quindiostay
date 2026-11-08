package co.edu.uniquindio.quindiostay.vista.comun;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatos para mostrar valores en las tablas. */
public final class Formato {

    private static final NumberFormat PESOS = NumberFormat.getIntegerInstance(new Locale("es", "CO"));
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Formato() {
    }

    /** 2600000 -> "$2.600.000" */
    public static String pesos(long valor) {
        return "$" + PESOS.format(valor);
    }

    /** 2026-11-06 -> "06/11/2026" */
    public static String fecha(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FECHA);
    }
}
