package co.edu.uniquindio.quindiostay.vista;

import java.util.List;

import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.logica.UsuarioSistemaService;
import co.edu.uniquindio.quindiostay.modelo.UsuarioSistema;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;

/** CRUD de usuarios internos. La pantalla la arma CrudPanel; Solo van las columnas y los botones. */
public class UsuariosSistemaPanel extends CrudPanel<UsuarioSistema> {
    private final UsuarioSistemaService servicio = new UsuarioSistemaService();

    public UsuariosSistemaPanel() {
        super("Usuarios del sistema", "ID", "Usuario", "Rol", "Prestador", "Estado");
        if (!Sesion.esAdmin()) {
            soloLectura();
        }
        cargar();
    }

    @Override
    protected List<UsuarioSistema> consultar(String filtro) throws Exception {
        return servicio.consultar(filtro);
    }

    @Override
    protected Object[] fila(UsuarioSistema u) {
        return new Object[] {
                u.id(), u.nombreUsuario(), u.rol(), u.prestador() == null ? "" : u.prestador(), u.estado()
        };
    }

    @Override
    protected void nuevo() {
        if (UsuarioSistemaDialog.mostrar(this, null)) {
            cargar();
            info("Usuario creado.");
        }
    }

    @Override
    protected void editar(UsuarioSistema u) {
        if (UsuarioSistemaDialog.mostrar(this, u)) {
            cargar();
            info("Usuario actualizado.");
        }
    }

    @Override
    protected void eliminar(UsuarioSistema u) throws Exception {
        servicio.eliminar(u);
    }

    @Override
    protected String describir(UsuarioSistema u) {
        return "el usuario \"" + u.nombreUsuario() + "\"";
    }
}
