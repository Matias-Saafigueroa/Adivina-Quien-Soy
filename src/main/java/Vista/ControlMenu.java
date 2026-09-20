package Vista;

import javax.swing.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;

/**
 * Lógica del menú principal (panelMenu de Jframe.form): cada botón avisa qué pantalla
 * se quiere abrir, y "Salir" cierra el programa. También deja el menú centrado en la
 * ventana, con un tamaño fijo, en vez de estirado a todo el ancho.
 */
public class ControlMenu {

    private final JPanel pantalla;

    public ControlMenu(JPanel panelMenu, JButton botonJugar, JButton botonEspectear, JButton botonMarcador,
                       JButton botonSalir, Runnable alJugar, Runnable alEspectear, Runnable alMarcador) {
        this.pantalla = centrar(panelMenu);

        botonJugar.addActionListener(e -> alJugar.run());
        botonEspectear.addActionListener(e -> alEspectear.run());
        botonMarcador.addActionListener(e -> alMarcador.run());
        botonSalir.addActionListener(e -> System.exit(0));
    }

    /** El panel que se agrega al CardLayout: el menú ya centrado. */
    public JPanel getPantalla() {
        return pantalla;
    }

    private static JPanel centrar(JPanel panelMenu) {
        for (Component componente : panelMenu.getComponents()) {
            if (componente instanceof JLabel titulo) {
                titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 26f));
                titulo.setHorizontalAlignment(SwingConstants.CENTER);
            }
        }
        panelMenu.setPreferredSize(new Dimension(340, 360));

        JPanel centrado = new JPanel(new GridBagLayout());
        centrado.add(panelMenu);
        // El CardLayout original lo ocultó por no ser la primera carta (y lo vuelve a ocultar al sacarlo
        // de ahí), así que se lo muestra recién después de moverlo: ahora la carta es el contenedor.
        panelMenu.setVisible(true);
        return centrado;
    }
}
