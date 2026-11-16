package co.edu.uniquindio.quindiostay.modelo;

import java.time.LocalDate;

/** Un pasajero de una reserva, con su edad el día de la salida (para ver quién es menor). */
public record PasajeroReserva(
        String nombre, 
        String documento, 
        LocalDate fechaNacimiento, 
        int edadEnSalida
) {
    public boolean esMenor() {
        return edadEnSalida < 18;
    }
}
