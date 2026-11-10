package co.edu.uniquindio.quindiostay.logica;

import co.edu.uniquindio.quindiostay.datos.Conexion;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Llena los reportes y las plantillas de JasperReports con datos de Oracle y los exporta a PDF.
 *
 * Los diseños (.jrxml) están en src/main/resources/reportes/:
 *   reporte01 ... reporte10        los 10 reportes (consulta SQL dentro de cada diseño)
 *   comprobante_reserva, comprobante_pago, factura,
 *   autorizacion_menores, autorizacion_datos   las plantillas de documentos
 * Cada diseño se compila una sola vez y se guarda en memoria.
 */
public final class GeneradorReportes {

    /** Un reporte del catálogo (lo que se muestra en la lista de la pantalla de reportes). */
    public record InfoReporte(String archivo, int numero, String nivel, String titulo, String pregunta) {
        @Override
        public String toString() {
            return numero + ". " + titulo;
        }
    }

    public static final Locale COLOMBIA = new Locale("es", "CO");
    private static final Map<String, JasperReport> COMPILADOS = new HashMap<>();

    private GeneradorReportes() {
    }

    /** Los 10 reportes, leídos de reportes/reportes.properties. */
    public static List<InfoReporte> catalogo() {
        Properties p = new Properties();
        try (InputStream in = GeneradorReportes.class.getResourceAsStream("/reportes/reportes.properties")) {
            if (in != null) {
                p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer reportes.properties", e);
        }
        List<InfoReporte> lista = new ArrayList<>();
        for (int n = 1; n <= 10; n++) {
            String archivo = String.format("reporte%02d", n);
            String[] partes = p.getProperty(archivo, "?|Reporte " + n + "|").split("\\|", 3);
            lista.add(new InfoReporte(archivo, n, partes[0], partes[1], partes.length > 2 ? partes[2] : ""));
        }
        return lista;
    }

    /** Compila el .jrxml la primera vez y lo deja en memoria. */
    private static synchronized JasperReport diseno(String archivo) throws JRException {
        JasperReport compilado = COMPILADOS.get(archivo);
        if (compilado == null) {
            try (InputStream in = GeneradorReportes.class.getResourceAsStream("/reportes/" + archivo + ".jrxml")) {
                if (in == null) {
                    throw new JRException("No se encontró el diseño " + archivo + ".jrxml en src/main/resources/reportes");
                }
                compilado = JasperCompileManager.compileReport(in);
            } catch (IOException e) {
                throw new JRException(e);
            }
            COMPILADOS.put(archivo, compilado);
        }
        return compilado;
    }

    /**
     * Ejecuta la consulta del diseño y arma el documento.
     *
     * @param archivo    nombre del .jrxml sin extensión, por ejemplo "reporte05" o "factura"
     * @param parametros por ejemplo Map.of("ID_RESERVA", 25); vacío para los reportes
     */
    public static JasperPrint generar(String archivo, Map<String, Object> parametros) throws JRException, SQLException {
        Map<String, Object> p = new HashMap<>(parametros);
        p.put(JRParameter.REPORT_LOCALE, COLOMBIA);
        try (Connection cn = Conexion.obtener()) {
            return JasperFillManager.fillReport(diseno(archivo), p, cn);
        }
    }

    public static void exportarPdf(JasperPrint documento, File destino) throws JRException {
        JasperExportManager.exportReportToPdfFile(documento, destino.getAbsolutePath());
    }

    /** Genera los 10 reportes en PDF dentro de una carpeta (para la entrega). Devuelve cuántos generó. */
    public static int exportarTodos(File carpeta) throws JRException, SQLException {
        int n = 0;
        for (InfoReporte r : catalogo()) {
            String nombre = String.format("Reporte_%02d_%s.pdf", r.numero(), r.titulo().replaceAll("[^A-Za-z0-9áéíóúñÁÉÍÓÚÑ ]", "").replace(' ', '_'));
            exportarPdf(generar(r.archivo(), Map.of()), new File(carpeta, nombre));
            n++;
        }
        return n;
    }
}
