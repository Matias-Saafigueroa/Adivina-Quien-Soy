import javax.swing.*;

/** Punto de entrada del programa: crea la ventana principal y la muestra. */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorada) {
            }
            JFrame ventana = new JFrame("Adiviná Quién Soy");
            ventana.setContentPane(new Jframe().getPanelPrincipal());
            ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ventana.pack();
            ventana.setLocationRelativeTo(null);
            ventana.setVisible(true);
        });
    }
}
