package co.edu.uniquindio.quindiostay.modelo;

import java.time.LocalDate;

/**
 * Una fila de la tabla PASAJERO 
 * Quien viaja; puede ser distinto del cliente que reserva.
 * @param id null cuando todavía no se ha guardado (es nuevo)
 */
public record Pasajero(
        Integer id,
        String nombres,
        String apellidos,
        String tipoDocumento,
        String numeroDocumento,
        LocalDate fechaNacimiento
) {
    public String nombreCompleto() {
        return nombres + " " + apellidos;
    }
}
