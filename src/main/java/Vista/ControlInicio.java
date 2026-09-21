package Vista;

import Modelos.Personaje;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Lógica de la pantalla de inicio (panelInicio de Jframe.form): el jugador escribe su
 * nombre y elige su personaje secreto entre las 23 tarjetas. "Empezar partida" se
 * habilita recién cuando hay nombre y personaje elegido.
 */
public class ControlInicio {

    private static final int COLUMNAS_TABLERO = 6;

    private final JTextField txtNombre;
    private final JLabel lblElegido;
    private final JButton btnEmpezar;
    private final ButtonGroup grupoPersonajes = new ButtonGroup();

    private Personaje personajeElegido;

    /**
     * @param alCancelar qué hacer al apretar Cancelar (volver al menú).
     * @param alEmpezar  qué hacer al apretar Empezar partida; recibe el nombre y el personaje elegido.
     */
    public ControlInicio(JTextField txtNombre, JLabel lblElegido, JPanel panelPersonajes,
                         JButton btnEmpezar, JButton btnCancelar, List<Personaje> personajes,
                         Runnable alCancelar, BiConsumer<String, Personaje> alEmpezar) {
        this.txtNombre = txtNombre;
        this.lblElegido = lblElegido;
        this.btnEmpezar = btnEmpezar;

        btnEmpezar.setEnabled(false);
        btnCancelar.addActionListener(e -> alCancelar.run());
        btnEmpezar.addActionListener(e -> alEmpezar.accept(txtNombre.getText().trim(), personajeElegido));

        txtNombre.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarBotonEmpezar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarBotonEmpezar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarBotonEmpezar();
            }
        });

        armarTablero(panelPersonajes, personajes);
    }

    /** Deja la pantalla como nueva: sin personaje elegido (el nombre escrito se conserva). */
    public void reiniciar() {
        grupoPersonajes.clearSelection();
        personajeElegido = null;
        lblElegido.setText("Elegido: (ninguno todavía)");
        actualizarBotonEmpezar();
    }

    /** Crea una tarjeta por cada personaje de personajes.txt. */
    private void armarTablero(JPanel panelPersonajes, List<Personaje> personajes) {
        JPanel tablero = new JPanel(new GridLayout(0, COLUMNAS_TABLERO, 6, 6));
        for (Personaje personaje : personajes) {
            TarjetaPersonaje tarjeta = new TarjetaPersonaje(personaje);
            tarjeta.addActionListener(e -> elegir(tarjeta.getPersonaje()));
            grupoPersonajes.add(tarjeta);
            tablero.add(tarjeta);
        }

        JScrollPane desplazable = new JScrollPane(tablero);
        desplazable.setBorder(null);
        desplazable.getVerticalScrollBar().setUnitIncrement(16);

        panelPersonajes.setLayout(new BorderLayout());
        panelPersonajes.add(desplazable, BorderLayout.CENTER);
    }

    private void elegir(Personaje personaje) {
        personajeElegido = personaje;
        lblElegido.setText("Elegido: " + TarjetaPersonaje.descripcion(personaje));
        actualizarBotonEmpezar();
    }

    private void actualizarBotonEmpezar() {
        btnEmpezar.setEnabled(personajeElegido != null && !txtNombre.getText().isBlank());
    }
}
