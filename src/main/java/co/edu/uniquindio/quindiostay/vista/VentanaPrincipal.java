package co.edu.uniquindio.quindiostay.vista;

import co.edu.uniquindio.quindiostay.logica.CompraService;
import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.logica.Sesion;
import co.edu.uniquindio.quindiostay.modelo.Usuario;
import co.edu.uniquindio.quindiostay.vista.comun.CrudPanel;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Ventana principal: menú según el rol y un área central donde se muestra
 * la pantalla elegida (CardLayout).
 *
 * Para conectar un CRUD nuevo solo hay que cambiar su línea en crearMenu():
 *   opcion(gestion, "Prestadores", PrestadoresPanel::new);
 * por
 *   opcion(gestion, "Prestadores", PrestadoresPanel::new);
 */
public class VentanaPrincipal extends JFrame {

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final Map<String, JComponent> abiertas = new HashMap<>();

    public VentanaPrincipal() {
        super("QuindioStay");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 720);
        setLocationRelativeTo(null);

        Usuario u = Sesion.actual();
        JLabel lblInicio = new JLabel("Bienvenido, " + u.nombre() + ". Elige una opción del menú.",
                SwingConstants.CENTER);
        lblInicio.putClientProperty("FlatLaf.styleClass", "h3");
        contenido.add(lblInicio, "inicio");

        JLabel lblSesion = new JLabel("Sesión: " + u.nombre() + "  ·  Rol: " + u.rol());
        lblSesion.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        setJMenuBar(crearMenu());
        add(contenido, BorderLayout.CENTER);
        add(lblSesion, BorderLayout.SOUTH);

        // El cliente ve sus reservas apenas entra
        if (Sesion.esCliente()) {
            abrir("Mis reservas", MisReservasPanel::new);
        }
    }

    private JMenuBar crearMenu() {
        JMenuBar barra = new JMenuBar();

        if (Sesion.esCliente()) {
            JMenu cuenta = new JMenu("Mi cuenta");
            opcion(cuenta, "Reservar un viaje", ReservarPanel::new);
            opcion(cuenta, "Mis reservas", MisReservasPanel::new);
            opcion(cuenta, "Mis notificaciones", MisNotificacionesPanel::new);
            opcion(cuenta, "Mi perfil", MiPerfilPanel::new);
            barra.add(cuenta);
        } else {
            JMenu gestion = new JMenu("Gestión");
            opcion(gestion, "Paquetes", PaquetesPanel::new);
            // ---- Juan José: reemplazar enConstruccion(...) por XxxPanel::new cuando esté listo ----
            if (Sesion.esAdmin()) {
                opcion(gestion, "Prestadores", PrestadoresPanel::new);
                opcion(gestion, "Destinos", DestinosPanel::new);
            }
            opcion(gestion, "Planes", () -> enConstruccion("Planes"));
            opcion(gestion, "Componentes", () -> enConstruccion("Componentes"));
            opcion(gestion, "Salidas", () -> enConstruccion("Salidas"));
            if (!Sesion.esEncargado()) {
                gestion.addSeparator();
                opcion(gestion, "Clientes", () -> enConstruccion("Clientes"));
                opcion(gestion, "Reservas y pagos", () -> enConstruccion("Reservas y pagos"));
                gestion.addSeparator();
                JMenuItem recordatorios = new JMenuItem("Generar recordatorios pendientes");
                recordatorios.addActionListener(e -> generarRecordatorios());
                gestion.add(recordatorios);
            }
            barra.add(gestion);

            if (!Sesion.esEncargado()) {
                JMenu reportes = new JMenu("Reportes");
                opcion(reportes, "Reportes y exportar PDF", ReportesPanel::new);
                opcion(reportes, "Documentos (comprobantes, factura, autorizaciones)", DocumentosPanel::new);
                barra.add(reportes);
            }

            if (Sesion.esAdmin()) {
                JMenu admin = new JMenu("Administración");
                opcion(admin, "Municipios", MunicipiosPanel::new);
                opcion(admin, "Tipos de prestador", TiposPrestadorPanel::new);
                opcion(admin, "Tipos de componente", TiposComponentePanel::new);
                opcion(admin, "Roles", RolesPanel::new);
                admin.addSeparator();
                opcion(admin, "Usuarios del sistema", UsuariosSistemaPanel::new);
                barra.add(admin);
            }
        }

        JMenu sesion = new JMenu("Sesión");
        JMenuItem salir = new JMenuItem("Cerrar sesión");
        salir.addActionListener(e -> cerrarSesion());
        sesion.add(salir);
        barra.add(sesion);
        return barra;
    }

    /** Agrega una opción al menú que abre la pantalla que crea "fabrica". */
    private void opcion(JMenu menu, String titulo, Supplier<JComponent> fabrica) {
        JMenuItem item = new JMenuItem(titulo);
        item.addActionListener(e -> abrir(titulo, fabrica));
        menu.add(item);
    }

    /** Muestra la pantalla; la crea solo la primera vez y después la recarga. */
    private void abrir(String titulo, Supplier<JComponent> fabrica) {
        JComponent pantalla = abiertas.get(titulo);
        if (pantalla == null) {
            pantalla = fabrica.get();
            abiertas.put(titulo, pantalla);
            contenido.add(pantalla, titulo);
        } else if (pantalla instanceof CrudPanel<?> crud) {
            crud.cargar();
        }
        tarjetas.show(contenido, titulo);
        setTitle("QuindioStay · " + titulo);
    }

    /** RN32: crea los recordatorios que falten según el perfil de cada cliente. */
    private void generarRecordatorios() {
        try {
            int n = new CompraService().generarRecordatorios();
            JOptionPane.showMessageDialog(this, n == 0 ? "No hay recordatorios pendientes por crear."
                    : "Se programaron " + n + " recordatorios según el perfil de cada cliente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, ErroresBD.mensaje(e), "Recordatorios", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JComponent enConstruccion(String titulo) {
        JLabel aviso = new JLabel(titulo + ": en construcción", SwingConstants.CENTER);
        aviso.putClientProperty("FlatLaf.styleClass", "h3");
        return aviso;
    }

    private void cerrarSesion() {
        Sesion.cerrar();
        dispose();
        new LoginFrame().setVisible(true);
    }
}
