package Vista;

import Modelos.ColorPelo;
import Modelos.Filtro;
import Modelos.Genero;
import Modelos.Personaje;
import Servicios.Partida;
import Servicios.Pregunta;

import javax.swing.*;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de la pantalla de juego (panelJuego de Jframe.form). El diseño está en el
 * form; acá solo se conectan los botones y se completa lo que cambia mientras se juega:
 * el tablero muestra los personajes que todavía pueden ser el de la máquina (los
 * descartados se apagan), los "comodines" son las preguntas por filtro, y hacer clic
 * en un personaje es arriesgar una adivinanza.
 */
public class ControlJuego {

    private static final int COLUMNAS_TABLERO = 6;
    private static final int FOTO_ANCHO = 60;
    private static final int FOTO_ALTO = 75;

    private final String archivoMarcador;
    private final Runnable alTerminar;

    private final JLabel lblRonda;
    private final JLabel lblCandidatos;
    private final JLabel lblTuPersonaje;
    private final JPanel tablero;
    private final JComboBox<String> comboPreguntas;
    private final JButton btnPreguntar;
    private final JTextArea registro;

    private final List<TarjetaPersonaje> tarjetas = new ArrayList<>();
    private final List<Pregunta> preguntasDisponibles = new ArrayList<>();
    private Partida partida;

    /** @param alTerminar qué hacer cuando la partida termina (por ejemplo, ir al marcador). */
    public ControlJuego(String archivoMarcador, Runnable alTerminar,
                        JLabel lblRonda, JLabel lblCandidatos, JLabel lblTuPersonaje, JPanel tablero,
                        JComboBox<String> comboPreguntas, JButton btnPreguntar, JButton btnRendirse,
                        JTextArea registro) {
        this.archivoMarcador = archivoMarcador;
        this.alTerminar = alTerminar;
        this.lblRonda = lblRonda;
        this.lblCandidatos = lblCandidatos;
        this.lblTuPersonaje = lblTuPersonaje;
        this.tablero = tablero;
        this.comboPreguntas = comboPreguntas;
        this.btnPreguntar = btnPreguntar;
        this.registro = registro;

        tablero.setLayout(new GridLayout(0, COLUMNAS_TABLERO, 6, 6));

        btnPreguntar.addActionListener(e -> preguntar());
        btnRendirse.addActionListener(e -> rendirse());
    }

    // ---------------------------------------------------------------
    // Inicio de la partida
    // ---------------------------------------------------------------

    public void empezar(String nombreUsuario, Personaje secreto, List<Personaje> personajes) {
        partida = new Partida(archivoMarcador, personajes, nombreUsuario, secreto);

        tablero.removeAll();
        tarjetas.clear();
        for (Personaje personaje : personajes) {
            TarjetaPersonaje tarjeta = new TarjetaPersonaje(personaje, FOTO_ANCHO, FOTO_ALTO);
            tarjeta.addActionListener(e -> adivinar(tarjeta));
            tarjetas.add(tarjeta);
            tablero.add(tarjeta);
        }

        lblTuPersonaje.setIcon(Imagenes.foto(secreto.getNombre(), 80, 100));
        lblTuPersonaje.setText(secreto.getNombre());

        registro.setText("");
        anotar(partida.mensajesDeInicio());
        cargarPreguntas();
        refrescar();
        tablero.revalidate();
        tablero.repaint();
    }

    /** Las 8 preguntas posibles, género (2), calvicie, lentes y color de pelo (4). */
    private void cargarPreguntas() {
        preguntasDisponibles.clear();
        preguntasDisponibles.add(new Pregunta(Filtro.GENERO, Genero.MASCULINO));
        preguntasDisponibles.add(new Pregunta(Filtro.GENERO, Genero.FEMENINO));
        preguntasDisponibles.add(new Pregunta(Filtro.CALVICIE, Boolean.TRUE));
        preguntasDisponibles.add(new Pregunta(Filtro.LENTES, Boolean.TRUE));
        preguntasDisponibles.add(new Pregunta(Filtro.COLOR_PELO, ColorPelo.COLORADO));
        preguntasDisponibles.add(new Pregunta(Filtro.COLOR_PELO, ColorPelo.NEGRO));
        preguntasDisponibles.add(new Pregunta(Filtro.COLOR_PELO, ColorPelo.AMARILLO));
        preguntasDisponibles.add(new Pregunta(Filtro.COLOR_PELO, ColorPelo.AZUL));

        comboPreguntas.removeAllItems();
        for (Pregunta pregunta : preguntasDisponibles) {
            comboPreguntas.addItem("¿Su personaje " + pregunta.getClausula() + "?");
        }
    }

    // ---------------------------------------------------------------
    // Acciones del jugador
    // ---------------------------------------------------------------

    private void preguntar() {
        int posicion = comboPreguntas.getSelectedIndex();
        if (posicion < 0) return;

        Pregunta pregunta = preguntasDisponibles.remove(posicion);
        comboPreguntas.removeItemAt(posicion);
        procesar(partida.preguntar(pregunta));
    }

    private void adivinar(TarjetaPersonaje tarjeta) {
        tarjeta.setSelected(false);
        int respuesta = JOptionPane.showConfirmDialog(tablero,
                "¿Estás seguro de que es " + tarjeta.getPersonaje().getNombre() + "?\nSi le errás, perdés el desafío.",
                "Arriesgar una adivinanza", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta == JOptionPane.YES_OPTION) {
            procesar(partida.adivinar(tarjeta.getPersonaje().getId()));
        }
    }

    private void rendirse() {
        int respuesta = JOptionPane.showConfirmDialog(tablero, "¿Seguro que querés rendirte?",
                "Rendirme", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            procesar(partida.rendirse());
        }
    }

    private void procesar(Partida.Turno turno) {
        anotar(turno.mensajes());
        switch (turno.fin()) {
            case SIGUE -> refrescar();
            case PASA_A_RONDA_2 -> {
                JOptionPane.showMessageDialog(tablero, turno.resumen(), "Ronda 2", JOptionPane.INFORMATION_MESSAGE);
                cargarPreguntas();
                refrescar();
            }
            case VICTORIA_TOTAL, DERROTA -> {
                JOptionPane.showMessageDialog(tablero, turno.resumen(), "Fin de la partida", JOptionPane.INFORMATION_MESSAGE);
                alTerminar.run();
            }
        }
    }

    // ---------------------------------------------------------------
    // Pantalla
    // ---------------------------------------------------------------

    /** Actualiza los rótulos y apaga en el tablero los personajes ya descartados. */
    private void refrescar() {
        lblRonda.setText("Ronda " + partida.getRonda() + ": " + partida.getNombreMaquina()
                + "  —  Turno " + partida.getTurno());

        List<Personaje> candidatos = partida.getCandidatosHumano();
        lblCandidatos.setText("Te quedan " + candidatos.size() + " personajes posibles.");
        for (TarjetaPersonaje tarjeta : tarjetas) {
            tarjeta.setEnabled(candidatos.contains(tarjeta.getPersonaje()));
        }
        btnPreguntar.setEnabled(comboPreguntas.getItemCount() > 0);
    }

    private void anotar(List<String> lineas) {
        for (String linea : lineas) {
            registro.append(linea + "\n");
        }
        registro.setCaretPosition(registro.getDocument().getLength());
    }
}
