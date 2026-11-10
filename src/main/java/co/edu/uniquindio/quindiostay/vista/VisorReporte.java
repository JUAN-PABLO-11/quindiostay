package co.edu.uniquindio.quindiostay.vista;

import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.logica.GeneradorReportes;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.File;
import java.util.Map;

/**
 * Ventana que muestra un reporte o una plantilla ya generados, con el botón "Exportar PDF".
 *
 * Uso más común (desde cualquier pantalla):
 *   VisorReporte.abrir(this, "factura", Map.of("ID_RESERVA", 25), "Factura reserva 25");
 */
public class VisorReporte extends JDialog {

    private final transient JasperPrint documento;
    private final String nombreArchivo;

    /** Genera el documento y lo muestra; si falla, muestra el error traducido. */
    public static void abrir(Component padre, String diseno, Map<String, Object> parametros, String titulo) {
        Cursor antes = padre.getCursor();
        padre.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            JasperPrint documento = GeneradorReportes.generar(diseno, parametros);
            new VisorReporte(SwingUtilities.getWindowAncestor(padre), titulo, documento).setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(padre, ErroresBD.mensaje(e), "No se pudo generar", JOptionPane.ERROR_MESSAGE);
        } finally {
            padre.setCursor(antes);
        }
    }

    /** Guarda el documento en PDF preguntando dónde. */
    public static void exportar(Component padre, JasperPrint documento, String nombreSugerido) {
        JFileChooser selector = new JFileChooser();
        selector.setFileFilter(new FileNameExtensionFilter("Documento PDF", "pdf"));
        selector.setSelectedFile(new File(nombreSugerido.replaceAll("[^A-Za-z0-9_ .-]", "") + ".pdf"));
        if (selector.showSaveDialog(padre) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File destino = selector.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".pdf")) {
            destino = new File(destino.getParentFile(), destino.getName() + ".pdf");
        }
        try {
            GeneradorReportes.exportarPdf(documento, destino);
            int abrir = JOptionPane.showConfirmDialog(padre, "PDF guardado en:\n" + destino.getAbsolutePath()
                    + "\n\n¿Quieres abrirlo?", "Exportar PDF", JOptionPane.YES_NO_OPTION);
            if (abrir == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(destino);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(padre, ErroresBD.mensaje(e), "No se pudo exportar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private VisorReporte(Window padre, String titulo, JasperPrint documento) {
        super(padre, titulo, ModalityType.MODELESS);
        this.documento = documento;
        this.nombreArchivo = titulo;

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.putClientProperty("FlatLaf.styleClass", "h3");
        JButton btnExportar = new JButton("Exportar PDF");
        btnExportar.addActionListener(e -> exportar(this, this.documento, nombreArchivo));

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        barra.add(lblTitulo, BorderLayout.WEST);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.add(btnExportar);
        barra.add(derecha, BorderLayout.EAST);

        add(barra, BorderLayout.NORTH);
        add(new JRViewer(documento), BorderLayout.CENTER);
        setSize(1000, 760);
        setLocationRelativeTo(padre);
    }
}
