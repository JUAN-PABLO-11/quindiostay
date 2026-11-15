package co.edu.uniquindio.quindiostay.modelo;

import java.time.LocalDate;

/**
 * Una fila de la tabla CLIENTE con datos de la persona; 
 * la cuenta de acceso vive en PERFIL_CLIENTE.
 * @param id null cuando todavía no se ha guardado (es nuevo)
 */
public record Cliente(
        Integer id,
        String nombres,
        String apellidos,
        String tipoDocumento,
        String numeroDocumento,
        String correo,
        String telefono,
        LocalDate fechaNacimiento,
        String ciudadOrigen
) {
    public String nombreCompleto() {
        return nombres + " " + apellidos;
    }
}
