package co.edu.uniquindio.quindiostay.vista;

import java.util.List;

import co.edu.uniquindio.quindiostay.logica.ClienteService;
import co.edu.uniquindio.quindiostay.modelo.Cliente;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;
import co.edu.uniquindio.quindiostay.vista.comun.Formato;

public class ClientesPanel extends CrudPanel<Cliente> {
    private final ClienteService servicio = new ClienteService();

    public ClientesPanel() {
        super("Clientes", "ID", "Nombre", "Documento", "Correo", "Teléfono", "Ciudad", "Nacimiento");
        cargar();
    }

    @Override
    protected List<Cliente> consultar(String filtro) throws Exception {
        return servicio.consultar(filtro);
    }

    @Override
    protected Object[] fila(Cliente c) {
        return new Object[] {
                c.id(), c.nombreCompleto(), c.tipoDocumento() + " " + c.numeroDocumento(), c.correo(),
                c.telefono(), c.ciudadOrigen(), Formato.fecha(c.fechaNacimiento())
        };
    }

    @Override
    protected void nuevo() {
        if (ClienteDialog.mostrar(this, null)) {
            cargar();
            info("Cliente creado.");
        }
    }

    @Override
    protected void editar(Cliente c) {
        if (ClienteDialog.mostrar(this, c)) {
            cargar();
            info("Cliente actualizado.");
        }
    }

    @Override
    protected void eliminar(Cliente c) throws Exception {
        servicio.eliminar(c);
    }

    @Override
    protected String describir(Cliente c) {
        return "al cliente \"" + c.nombreCompleto() + "\"";
    }
}
