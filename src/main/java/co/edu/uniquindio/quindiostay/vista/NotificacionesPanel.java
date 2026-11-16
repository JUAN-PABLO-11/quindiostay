package co.edu.uniquindio.quindiostay.vista;

import java.util.List;

import co.edu.uniquindio.quindiostay.logica.NotificacionGestionService;
import co.edu.uniquindio.quindiostay.modelo.NotificacionGestion;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;
import co.edu.uniquindio.quindiostay.vista.comun.Formato;

/** CRUD de notificaciones. La pantalla la arma CrudPanel. */
public class NotificacionesPanel extends CrudPanel<NotificacionGestion>{
    private final NotificacionGestionService servicio = new NotificacionGestionService();

    public NotificacionesPanel() {
        super("Notificaciones", "ID", "Programada", "Cliente", "Tipo", "Canal", "Estado", "Reserva", "Mensaje");
        cargar();
    }

    @Override
    protected List<NotificacionGestion> consultar(String filtro) throws Exception {
        return servicio.consultar(filtro);
    }

    @Override
    protected Object[] fila(NotificacionGestion n) {
        return new Object[] {
                n.id(), Formato.fechaHora(n.fechaProgramada()), n.cliente(), n.tipo(), n.canal(), n.estado(),
                n.idReserva() == null ? "" : n.idReserva(), n.mensaje()
        };
    }

    @Override
    protected void nuevo() {
        if (NotificacionGestionDialog.mostrar(this, null)) {
            cargar();
            info("Notificación creada.");
        }
    }

    @Override
    protected void editar(NotificacionGestion n) {
        if (NotificacionGestionDialog.mostrar(this, n)) {
            cargar();
            info("Notificación actualizada.");
        }
    }

    @Override
    protected void eliminar(NotificacionGestion n) throws Exception {
        servicio.eliminar(n);
    }

    @Override
    protected String describir(NotificacionGestion n) {
        return "la notificación " + n.id() + " para " + n.cliente();
    }
}
