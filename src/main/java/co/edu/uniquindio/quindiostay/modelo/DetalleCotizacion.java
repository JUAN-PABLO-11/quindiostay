package co.edu.uniquindio.quindiostay.modelo;

import java.time.LocalDate;

/**
 * entidad débil de COTIZACION: su llave es cotización + número de línea). 
 * "componente" viene del JOIN.
 * @param numeroLinea 1, 2, 3... Se renumera al guardar, así que en pantalla solo es informativo
 * @param valor       valor de la línea para esas personas (normalmente costo del componente x personas)
 */
public record DetalleCotizacion(
        int numeroLinea,
        LocalDate fechaServicio,
        int cantidadPersonas,
        int valor,
        int idComponente,
        String componente
) {
    
}
