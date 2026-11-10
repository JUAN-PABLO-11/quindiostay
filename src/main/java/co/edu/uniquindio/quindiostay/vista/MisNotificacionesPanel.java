package co.edu.uniquindio.quindiostay.vista;

import co.edu.uniquindio.quindiostay.logica.CuentaService;
import co.edu.uniquindio.quindiostay.modelo.Notificacion;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;
import co.edu.uniquindio.quindiostay.vista.comun.Formato;

import java.util.List;

/** Historial de notificaciones del cliente (recordatorios, confirmaciones, promociones). Solo consulta. */
public class MisNotificacionesPanel extends CrudPanel<Notificacion> {

    private final CuentaService servicio = new CuentaService();

    public MisNotificacionesPanel() {
        super("Mis notificaciones", "Programada", "Tipo", "Canal", "Mensaje", "Estado", "Enviada");
        soloLectura();
        tabla.getColumnModel().getColumn(3).setPreferredWidth(420);
        cargar();
    }

    @Override
    protected List<Notificacion> consultar(String filtro) throws Exception {
        return servicio.misNotificaciones(filtro);
    }

    @Override
    protected Object[] fila(Notificacion n) {
        return new Object[] {Formato.fechaHora(n.programada()), n.tipo(), n.canal(), n.mensaje(), n.estado(),
                Formato.fechaHora(n.enviada())};
    }

    @Override
    protected void nuevo() {
    }

    @Override
    protected void editar(Notificacion n) {
    }

    @Override
    protected void eliminar(Notificacion n) {
    }

    @Override
    protected String describir(Notificacion n) {
        return "la notificación";
    }
}
