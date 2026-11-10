package co.edu.uniquindio.quindiostay.modelo;

/**
 * Lo que el cliente escribe en la pasarela simulada. Los datos de la tarjeta
 * solo se validan: nunca se guardan en la base de datos.
 */
public record DatosPago(String metodo, long monto, String numeroTarjeta, String titular, String vencimiento,
                        String cvv, String banco) {
}
