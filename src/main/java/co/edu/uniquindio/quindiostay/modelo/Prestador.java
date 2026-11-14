package co.edu.uniquindio.quindiostay.modelo;

public record Prestador(
    Integer id,
    String nombreComercial,
    String nit,
    String numeroRnt,
    String direccion,
    String telefono,
    String correo,
    String estado,
    int idTipoPrestador,
    String tipoPrestador,
    int idMunicipio,
    String municipio
) {
}
