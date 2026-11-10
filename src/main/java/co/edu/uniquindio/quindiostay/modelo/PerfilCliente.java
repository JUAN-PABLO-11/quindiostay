package co.edu.uniquindio.quindiostay.modelo;

/** Datos que el cliente ve y edita en "Mi perfil". */
public record PerfilCliente(
        int idCliente,
        String nombre,
        String documento,
        String correo,
        String telefono,
        String ciudadOrigen,
        String idioma,
        String canalPreferido,
        int diasAnticipacion,
        boolean aceptaPromociones) {
}
