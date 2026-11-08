package co.edu.uniquindio.quindiostay.modelo;

/**
 * Elemento de un JComboBox: guarda el id (lo que va a la base de datos)
 * y el texto (lo que ve el usuario). Sirve para cualquier llave foránea.
 */
public record Opcion(int id, String texto) {
    @Override
    public String toString() {
        return texto;
    }
}
