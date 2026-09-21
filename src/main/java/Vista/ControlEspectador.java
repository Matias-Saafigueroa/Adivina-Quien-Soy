package Vista;

import Modelos.Personaje;
import Servicios.Espectador;

import javax.swing.*;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Lógica del modo espectador (panelEspectador de Jframe.form): se ve, paso a paso,
 * cómo juegan la Máquina 1 y la Máquina 2. Cada máquina tiene su propio tablero con
 * los 23 personajes: los que ya descartó se apagan, igual que en el tablero del jugador.
 * Cada clic en "Siguiente paso" hace jugar a una máquina (un paso por vez, para poder seguir la partida).
 */
public class ControlEspectador {

    private static final int COLUMNAS_TABLERO = 6;
    private static final int FOTO_ANCHO = 45;
    private static final int FOTO_ALTO = 48;

    /** Los componentes de una de las dos columnas de la pantalla (una por máquina). */
    public static class Lado {
        private final JLabel nombre;
        private final JLabel secreto;
        private final JLabel candidatos;
        private final JPanel tablero;
        private final List<TarjetaPersonaje> tarjetas = new ArrayList<>();

        public Lado(JLabel nombre, JLabel secreto, JLabel candidatos, JPanel tablero) {
            this.nombre = nombre;
            this.secreto = secreto;
            this.candidatos = candidatos;
            this.tablero = tablero;
            tablero.setLayout(new GridLayout(0, COLUMNAS_TABLERO, 4, 4));
        }

        /** Arma el tablero con las 23 tarjetas y muestra el personaje secreto de esta máquina. */
        void armar(String nombreMaquina, Personaje personajeSecreto, List<Personaje> personajes) {
            nombre.setText(nombreMaquina);
            secreto.setIcon(Imagenes.foto(personajeSecreto.getNombre(), FOTO_ANCHO, FOTO_ALTO));
            secreto.setText("Su personaje: " + personajeSecreto.getNombre());

            tablero.removeAll();
            tarjetas.clear();
            for (Personaje personaje : personajes) {
                TarjetaPersonaje tarjeta = new TarjetaPersonaje(personaje, FOTO_ANCHO, FOTO_ALTO);
                tarjeta.soloMostrar();
                tarjetas.add(tarjeta);
                tablero.add(tarjeta);
            }
            tablero.revalidate();
            tablero.repaint();
        }

        /** Apaga las tarjetas de los personajes que la máquina ya descartó. */
        void refrescar(List<Personaje> quedan, String rival) {
            candidatos.setText("Busca el personaje de " + rival + ": le quedan " + quedan.size() + " candidato(s).");
            for (TarjetaPersonaje tarjeta : tarjetas) {
                tarjeta.setEnabled(quedan.contains(tarjeta.getPersonaje()));
            }
        }

        /** Al terminar, resalta el personaje que esta máquina estaba buscando (los descartados siguen apagados). */
        void revelar(Personaje buscado) {
            for (TarjetaPersonaje tarjeta : tarjetas) {
                if (tarjeta.getPersonaje().getId() == buscado.getId()) {
                    tarjeta.marcarComoSecreto();
                }
            }
        }
    }

    private final Lado izquierda;
    private final Lado derecha;
    private final JTextArea registro;
    private final JButton btnSiguiente;

    private List<Personaje> personajes;
    private Espectador espectador;

    public ControlEspectador(Lado izquierda, Lado derecha, JTextArea registro,
                             JButton btnSiguiente, JButton btnNueva, JButton btnVolver, Runnable alVolver) {
        this.izquierda = izquierda;
        this.derecha = derecha;
        this.registro = registro;
        this.btnSiguiente = btnSiguiente;

        btnSiguiente.addActionListener(e -> siguientePaso());
        btnNueva.addActionListener(e -> nuevaPartida(personajes));
        btnVolver.addActionListener(e -> alVolver.run());
    }

    /** Arma una partida nueva entre las dos máquinas. */
    public void nuevaPartida(List<Personaje> personajes) {
        this.personajes = personajes;
        espectador = new Espectador(personajes);

        izquierda.armar(espectador.getNombreMaquina1(), espectador.getSecretoMaquina1(), personajes);
        derecha.armar(espectador.getNombreMaquina2(), espectador.getSecretoMaquina2(), personajes);
        refrescar();

        registro.setText("");
        anotar(espectador.mensajesDeInicio());
        btnSiguiente.setEnabled(true);
    }

    private void siguientePaso() {
        anotar(espectador.siguientePaso());
        refrescar();
        if (espectador.isTerminada()) {
            btnSiguiente.setEnabled(false);
            // Cada máquina buscaba el personaje de la otra.
            izquierda.revelar(espectador.getSecretoMaquina2());
            derecha.revelar(espectador.getSecretoMaquina1());
            // Después de aceptar el cartel quedan los botones "Nueva partida" y "Volver al menú".
            JOptionPane.showMessageDialog(registro, "¡Ganó " + espectador.getGanador() + "!",
                    "Fin de la partida", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void refrescar() {
        izquierda.refrescar(espectador.getCandidatosMaquina1(), espectador.getNombreMaquina2());
        derecha.refrescar(espectador.getCandidatosMaquina2(), espectador.getNombreMaquina1());
    }

    private void anotar(List<String> lineas) {
        for (String linea : lineas) {
            registro.append(linea + "\n");
        }
        registro.setCaretPosition(registro.getDocument().getLength());
    }
}
