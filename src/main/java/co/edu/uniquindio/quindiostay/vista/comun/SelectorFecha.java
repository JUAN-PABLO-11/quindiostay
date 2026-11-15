package co.edu.uniquindio.quindiostay.vista.comun;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Selector de fecha con tres listas (día, mes, año). 
 * No permite fechas imposibles (EJ: 31 de febrero) 
 * ni errores de formato: el día se ajusta solo a los días del mes.
 * Se usa como cualquier campo: f.campo("Fecha de nacimiento", selector).
 */
public class SelectorFecha extends JPanel{
    private static final String[] MESES = {
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    };

    private final JComboBox<Integer> cmbDia = new JComboBox<>();
    private final JComboBox<String> cmbMes = new JComboBox<>(MESES);
    private final DefaultComboBoxModel<Integer> modeloAnios = new DefaultComboBoxModel<>();
    private final JComboBox<Integer> cmbAnio = new JComboBox<>(modeloAnios);
    private boolean ajustando = false;

    /**
     * @param anioDesde primer año de la lista
     * @param anioHasta último año de la lista (si setFecha recibe un año fuera del rango, se agrega)
     */
    public SelectorFecha(int anioDesde, int anioHasta) {
        super(new FlowLayout(FlowLayout.LEFT, 4, 0));
        for (int a = anioDesde; a <= anioHasta; a++) {
            modeloAnios.addElement(a);
        }
        add(cmbDia);
        add(cmbMes);
        add(cmbAnio);
        cmbMes.addActionListener(e -> ajustarDias());
        cmbAnio.addActionListener(e -> ajustarDias());
        setFecha(LocalDate.now().isBefore(LocalDate.of(anioDesde, 1, 1))
                ? LocalDate.of(anioDesde, 1, 1) : LocalDate.now());
    }

    /** La fecha elegida (nunca null). */
    public LocalDate getFecha() {
        return LocalDate.of((Integer) cmbAnio.getSelectedItem(),
                cmbMes.getSelectedIndex() + 1, (Integer) cmbDia.getSelectedItem());
    }

    /** Pone la fecha; si su año no está en la lista, lo agrega en su lugar. */
    public void setFecha(LocalDate fecha) {
        ajustando = true;
        try {
            asegurarAnio(fecha.getYear());
            cmbAnio.setSelectedItem(fecha.getYear());
            cmbMes.setSelectedIndex(fecha.getMonthValue() - 1);
        } finally {
            ajustando = false;
        }
        llenarDias(fecha.getDayOfMonth());
    }

    @Override
    public void setEnabled(boolean habilitado) {
        super.setEnabled(habilitado);
        cmbDia.setEnabled(habilitado);
        cmbMes.setEnabled(habilitado);
        cmbAnio.setEnabled(habilitado);
    }

    private void asegurarAnio(int anio) {
        for (int i = 0; i < modeloAnios.getSize(); i++) {
            int existente = modeloAnios.getElementAt(i);
            if (existente == anio) {
                return;
            }
            if (existente > anio) {
                modeloAnios.insertElementAt(anio, i);
                return;
            }
        }
        modeloAnios.addElement(anio);
    }

    /** Cuando cambia el mes o el año, la lista de días se rehace conservando el día si todavía existe. */
    private void ajustarDias() {
        if (ajustando || cmbDia.getSelectedItem() == null) {
            return;
        }
        llenarDias((Integer) cmbDia.getSelectedItem());
    }

    private void llenarDias(int diaDeseado) {
        Integer anio = (Integer) cmbAnio.getSelectedItem();
        if (anio == null) {
            return;
        }
        int dias = YearMonth.of(anio, cmbMes.getSelectedIndex() + 1).lengthOfMonth();
        ajustando = true;
        try {
            cmbDia.removeAllItems();
            for (int d = 1; d <= dias; d++) {
                cmbDia.addItem(d);
            }
            cmbDia.setSelectedItem(Math.min(diaDeseado, dias));
        } finally {
            ajustando = false;
        }
    }
}
