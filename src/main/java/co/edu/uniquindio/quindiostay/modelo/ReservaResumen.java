package co.edu.uniquindio.quindiostay.modelo;

import java.time.LocalDate;

/** Una reserva vista por el cliente en "Mis reservas" (solo lectura). */
public record ReservaResumen(
        int id,
        String paquete,
        String plan,
        LocalDate fechaSalida,
        String estado,
        int pasajeros,
        long valorTotal,
        long pagado) {

    public long saldo() {
        return valorTotal - pagado;
    }
}
