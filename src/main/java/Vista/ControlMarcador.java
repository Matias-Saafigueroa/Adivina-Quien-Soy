package Vista;

import DataSystem.GuardarMarcadorTXT;
import Modelos.RegistroMarcador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Map;

/** Lógica de la pantalla del marcador (panelMarcador de Jframe.form): llena la tabla con marcador.txt. */
public class ControlMarcador {

    private static final String[] COLUMNAS = {
            "Jugador", "Le ganó a Máquina 1", "Desafíos completos (las 2)", "Derrotas", "Mejor desafío (intentos)"};

    private final GuardarMarcadorTXT marcador;
    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    public ControlMarcador(String archivoMarcador, JTable tabla, JButton btnVolver, Runnable alVolver) {
        this.marcador = new GuardarMarcadorTXT(archivoMarcador);
        tabla.setModel(modelo);
        btnVolver.addActionListener(e -> alVolver.run());
    }

    /** Vuelve a leer el archivo y redibuja la tabla. */
    public void actualizar() {
        modelo.setRowCount(0);
        for (Map.Entry<String, RegistroMarcador> fila : marcador.leer().entrySet()) {
            RegistroMarcador r = fila.getValue();
            modelo.addRow(new Object[]{
                    fila.getKey(),
                    r.getVictoriasMaquina1(),
                    r.getVictoriasCompletas(),
                    r.getDerrotas(),
                    r.tieneVictoriasCompletas() ? r.getMejorIntentos() : "-"});
        }
    }
}
