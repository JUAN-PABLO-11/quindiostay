package co.edu.uniquindio.quindiostay.modelo;

import java.time.LocalDate;
import java.time.Period;

/** Pasajero que el cliente agrega al reservar. */
public record PasajeroNuevo(String nombres, String apellidos, String tipoDocumento, String numeroDocumento,
                            LocalDate fechaNacimiento) {

    public int edadEn(LocalDate fecha) {
        return Period.between(fechaNacimiento, fecha).getYears();
    }

    public boolean esAdultoEn(LocalDate fecha) {
        return edadEn(fecha) >= 18;
    }

    public String nombreCompleto() {
        return nombres + " " + apellidos;
    }
}
