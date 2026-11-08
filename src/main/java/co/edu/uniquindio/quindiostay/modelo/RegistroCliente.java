package co.edu.uniquindio.quindiostay.modelo;

import java.time.LocalDate;

/** Datos del formulario "Crear cuenta": van a CLIENTE y a PERFIL_CLIENTE. */
public record RegistroCliente(
        String nombres,
        String apellidos,
        String tipoDocumento,
        String numeroDocumento,
        String correo,
        String telefono,
        LocalDate fechaNacimiento,
        String ciudadOrigen,
        String idioma,
        String canalPreferido,
        int diasAnticipacion,
        boolean aceptaPromociones) {
}
