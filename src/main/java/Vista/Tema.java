package Vista;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Colores de Los Simpson para toda la ventana: cielo azul de fondo, amarillo Simpson
 * en botones y títulos, naranja al pasar el mouse o elegir. Se aplica de una sola vez
 * con Tema.aplicar(...), así el form no se llena de colores sueltos.
 */
public final class Tema {

    public static final Color CIELO = new Color(0x8FD6F7);
    public static final Color CIELO_CLARO = new Color(0xC4EAFB);
    public static final Color AMARILLO = new Color(0xFED90F);
    public static final Color AMARILLO_APAGADO = new Color(0xE3D98F);
    public static final Color NARANJA = new Color(0xF28C28);
    public static final Color TINTA = new Color(0x1F3A5F);
    public static final Color CREMA = new Color(0xFFF6CC);

    private Tema() {
    }

    /** Pinta el componente y todo lo que tiene adentro. Las tarjetas de personajes se pintan solas. */
    public static void aplicar(Component componente) {
        instalarDialogos();
        pintar(componente);
    }

    private static void instalarDialogos() {
        UIManager.put("OptionPane.background", CIELO);
        UIManager.put("Panel.background", CIELO);
        UIManager.put("OptionPane.messageForeground", TINTA);
    }

    private static void pintar(Component c) {
        if (c instanceof TarjetaPersonaje) return;

        if (c instanceof JButton boton) {
            pintarBoton(boton);
        } else if (c instanceof JLabel etiqueta) {
            pintarEtiqueta(etiqueta);
        } else if (c instanceof JTextArea area) {
            area.setBackground(CREMA);
            area.setForeground(TINTA);
        } else if (c instanceof JTextField campo) {
            campo.setBackground(CREMA);
            campo.setForeground(TINTA);
        } else if (c instanceof JComboBox<?> combo) {
            combo.setBackground(CREMA);
            combo.setForeground(TINTA);
        } else if (c instanceof JTable tabla) {
            pintarTabla(tabla);
        } else if (c instanceof JScrollPane desplazable) {
            desplazable.setBorder(BorderFactory.createLineBorder(TINTA, 2));
            desplazable.getViewport().setBackground(CIELO_CLARO);
        } else if (c instanceof JPanel panel) {
            boolean dentroDeUnDesplazable = panel.getParent() instanceof JViewport;
            panel.setOpaque(true);
            panel.setBackground(dentroDeUnDesplazable ? CIELO_CLARO : CIELO);
        }

        if (c instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) {
                pintar(hijo);
            }
        }
    }

    private static void pintarBoton(JButton boton) {
        Border marco = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TINTA, 2),
                BorderFactory.createEmptyBorder(5, 12, 5, 12));
        boton.setContentAreaFilled(false);
        boton.setOpaque(true);
        boton.setBorder(marco);
        boton.setFocusPainted(false);
        boton.setForeground(TINTA);
        boton.setFont(boton.getFont().deriveFont(Font.BOLD));
        boton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        boton.setBackground(boton.isEnabled() ? AMARILLO : AMARILLO_APAGADO);
        boton.addPropertyChangeListener("enabled", e -> boton.setBackground(boton.isEnabled() ? AMARILLO : AMARILLO_APAGADO));
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (boton.isEnabled()) boton.setBackground(NARANJA);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(boton.isEnabled() ? AMARILLO : AMARILLO_APAGADO);
            }
        });
    }

    /** Los rótulos grandes (los de 16 puntos o más) son títulos: van en un cartel amarillo. */
    private static void pintarEtiqueta(JLabel etiqueta) {
        etiqueta.setForeground(TINTA);
        if (etiqueta.getFont().getSize() >= 16) {
            etiqueta.setOpaque(true);
            etiqueta.setBackground(AMARILLO);
            etiqueta.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(TINTA, 2),
                    BorderFactory.createEmptyBorder(3, 10, 3, 10)));
        }
    }

    private static void pintarTabla(JTable tabla) {
        tabla.setBackground(Color.WHITE);
        tabla.setForeground(TINTA);
        tabla.setGridColor(TINTA);
        tabla.setRowHeight(24);
        tabla.setSelectionBackground(NARANJA);
        tabla.setSelectionForeground(Color.WHITE);

        DefaultTableCellRenderer encabezado = new DefaultTableCellRenderer();
        encabezado.setOpaque(true);
        encabezado.setBackground(AMARILLO);
        encabezado.setForeground(TINTA);
        encabezado.setFont(tabla.getFont().deriveFont(Font.BOLD));
        encabezado.setBorder(BorderFactory.createLineBorder(TINTA, 1));
        encabezado.setHorizontalAlignment(SwingConstants.CENTER);
        tabla.getTableHeader().setDefaultRenderer(encabezado);
    }
}
