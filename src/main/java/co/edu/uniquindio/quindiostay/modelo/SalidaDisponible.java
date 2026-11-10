package co.edu.uniquindio.quindiostay.modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Salida futura de un paquete con los cupos que le quedan (para reservar). */
public record SalidaDisponible(int id, LocalDate fecha, int cupo, int vendidos) {

    public int disponibles() {
        return cupo - vendidos;
    }

    @Override
    public String toString() {
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "  ·  "
                + (disponibles() > 0 ? disponibles() + " cupos libres" : "agotada");
    }
}
