package co.edu.uniquindio.quindiostay.modelo;

import co.edu.uniquindio.quindiostay.vista.comun.Formato;

/** Plan activo y vigente en la fecha de una salida (RN10). */
public record PlanVigente(int id, String nombre, String tipo, long precio, String descripcion) {
    @Override
    public String toString() {
        return nombre + "  ·  " + Formato.pesos(precio) + " por persona";
    }
}
