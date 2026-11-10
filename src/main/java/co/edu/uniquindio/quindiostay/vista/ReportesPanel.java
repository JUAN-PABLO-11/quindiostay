package co.edu.uniquindio.quindiostay.vista;

import co.edu.uniquindio.quindiostay.logica.ErroresBD;
import co.edu.uniquindio.quindiostay.logica.GeneradorReportes;
import co.edu.uniquindio.quindiostay.logica.GeneradorReportes.InfoReporte;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.Map;

/**
 * Los 10 reportes: lista a la izquierda; a la derecha la pregunta de negocio,
 * el reporte generado (con sus gráficos) y los botones para exportar a PDF.
 */
public class ReportesPanel extends JPanel {

    private final JList<InfoReporte> lista = new JList<>(GeneradorReportes.catalogo().toArray(InfoReporte[]::new));
    private final JLabel lblTitulo = new JLabel("Elige un reporte");
    private final JLabel lblPregunta = new JLabel(" ");
    private final JLabel lblEstado = new JLabel(" ");
    private final JPanel visor = new JPanel(new BorderLayout());
    private transient JasperPrint actual;

    public ReportesPanel() {
        super(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));

        JLabel titulo = new JLabel("Reportes");
        titulo.putClientProperty("FlatLaf.styleClass", "h2");

        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foco) {
                InfoReporte r = (InfoReporte) v;
                JLabel c = (JLabel) super.getListCellRendererComponent(l, v, i, sel, foco);
                c.setText("<html><b>" + r.numero() + ". " + r.titulo() + "</b><br><small>" + r.nivel() + "</small></html>");
                c.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                return c;
            }
        });
        lista.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && lista.getSelectedValue() != null) {
                InfoReporte r = lista.getSelectedValue();
                lblTitulo.setText(r.numero() + ". " + r.titulo() + "  ·  " + r.nivel());
                lblPregunta.setText(r.pregunta());
                generar();
            }
        });

        JButton btnActualizar = new JButton("Actualizar");
        JButton btnExportar = new JButton("Exportar PDF");
        JButton btnTodos = new JButton("Exportar los 10 a una carpeta");
        btnActualizar.addActionListener(e -> generar());
        btnExportar.addActionListener(e -> {
            if (actual != null) {
                InfoReporte r = lista.getSelectedValue();
                VisorReporte.exportar(this, actual, String.format("Reporte %02d %s", r.numero(), r.titulo()));
            }
        });
        btnTodos.addActionListener(e -> exportarTodos());

        lblTitulo.putClientProperty("FlatLaf.styleClass", "h3");
        lblPregunta.setForeground(new Color(0x5F6368));
        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.add(lblTitulo);
        textos.add(lblPregunta);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botones.add(btnActualizar);
        botones.add(btnExportar);
        botones.add(btnTodos);
        JPanel cabeza = new JPanel(new BorderLayout());
        cabeza.add(textos, BorderLayout.CENTER);
        cabeza.add(botones, BorderLayout.EAST);

        visor.add(new JLabel("Selecciona un reporte de la lista.", SwingConstants.CENTER));
        JPanel derecha = new JPanel(new BorderLayout(0, 8));
        derecha.add(cabeza, BorderLayout.NORTH);
        derecha.add(visor, BorderLayout.CENTER);

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(lista), derecha);
        division.setDividerLocation(330);

        add(titulo, BorderLayout.NORTH);
        add(division, BorderLayout.CENTER);
        add(lblEstado, BorderLayout.SOUTH);
    }

    private void generar() {
        InfoReporte r = lista.getSelectedValue();
        if (r == null) {
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            actual = GeneradorReportes.generar(r.archivo(), Map.of());
            visor.removeAll();
            visor.add(new JRViewer(actual), BorderLayout.CENTER);
            visor.revalidate();
            visor.repaint();
            lblEstado.setForeground(new Color(0x202124));
            lblEstado.setText("Reporte generado: " + actual.getPages().size() + " página(s).");
        } catch (Exception e) {
            actual = null;
            lblEstado.setForeground(new Color(0xC62828));
            lblEstado.setText(ErroresBD.mensaje(e));
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void exportarTodos() {
        JFileChooser selector = new JFileChooser();
        selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        selector.setDialogTitle("Carpeta donde guardar los 10 PDF");
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            int n = GeneradorReportes.exportarTodos(selector.getSelectedFile());
            JOptionPane.showMessageDialog(this, n + " reportes guardados en:\n" + selector.getSelectedFile().getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, ErroresBD.mensaje(e), "No se pudo exportar", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
}
