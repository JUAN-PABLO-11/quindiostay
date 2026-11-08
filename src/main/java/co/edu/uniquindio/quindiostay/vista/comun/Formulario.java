package co.edu.uniquindio.quindiostay.vista.comun;

import co.edu.uniquindio.quindiostay.modelo.Opcion;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/**
 * Panel de formulario en dos columnas (etiqueta | campo) sin pelear con GridBagLayout.
 *
 * Uso:
 *   Formulario f = new Formulario();
 *   f.campo("Nombre", txtNombre);
 *   JTextArea txtDescripcion = f.areaTexto("Descripción", 4);
 */
public class Formulario extends JPanel {

    private int fila = 0;

    public Formulario() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(14, 16, 6, 16));
    }

    /** Agrega una fila "etiqueta: componente" y devuelve el mismo componente. */
    public <C extends JComponent> C campo(String etiqueta, C componente) {
        agregar(etiqueta, componente, GridBagConstraints.WEST);
        return componente;
    }

    /** Agrega un área de texto de varias líneas (con scroll) y la devuelve. */
    public JTextArea areaTexto(String etiqueta, int filas) {
        JTextArea area = new JTextArea(filas, 30);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        agregar(etiqueta, new JScrollPane(area), GridBagConstraints.NORTHWEST);
        return area;
    }

    private void agregar(String etiqueta, JComponent componente, int anclaEtiqueta) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = fila;
        g.anchor = anclaEtiqueta;
        g.insets = new Insets(5, 0, 5, 12);
        add(new JLabel(etiqueta), g);

        g = new GridBagConstraints();
        g.gridx = 1;
        g.gridy = fila;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(5, 0, 5, 0);
        add(componente, g);
        fila++;
    }

    // ------------------------------------------------------------------
    // Utilidades para combos de llaves foráneas
    // ------------------------------------------------------------------

    /** Llena un combo con las opciones de un CatalogoDAO. */
    public static void llenar(JComboBox<Opcion> combo, List<Opcion> opciones) {
        combo.removeAllItems();
        for (Opcion o : opciones) {
            combo.addItem(o);
        }
        combo.setSelectedIndex(-1);
    }

    /** Selecciona la opción con ese id; si no está en la lista, la agrega. */
    public static void seleccionar(JComboBox<Opcion> combo, int id, String texto) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).id() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        Opcion extra = new Opcion(id, texto);
        combo.addItem(extra);
        combo.setSelectedItem(extra);
    }

    /** Id de la opción seleccionada, o 0 si no hay ninguna. */
    public static int idSeleccionado(JComboBox<Opcion> combo) {
        Object item = combo.getSelectedItem();
        return item instanceof Opcion o ? o.id() : 0;
    }

    /** Texto de la opción seleccionada, o "" si no hay ninguna. */
    public static String textoSeleccionado(JComboBox<?> combo) {
        Object item = combo.getSelectedItem();
        return item == null ? "" : item.toString();
    }
}
