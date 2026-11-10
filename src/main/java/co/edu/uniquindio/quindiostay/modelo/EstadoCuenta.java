package co.edu.uniquindio.quindiostay.modelo;

/** Lo que vale una reserva, lo pagado y su saldo (vista v_pago_reserva). */
public record EstadoCuenta(int idReserva, int idCliente, String estado, long valorTotal, long pagado) {
    public long saldo() {
        return valorTotal - pagado;
    }
}
