package Servicios;

import DataSystem.GuardarMarcadorTXT;
import Modelos.Personaje;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Partida del jugador contra las dos máquinas, pensada para la interfaz Swing.
 * En vez de un bucle que espera una entrada, cada acción del jugador (preguntar,
 * adivinar, rendirse) es un método que devuelve qué pasó, y la ventana lo muestra.
 *
 * Ronda 1: contra la Máquina 1. Si le gana, pasa a la Ronda 2 contra la Máquina 2
 * con el mismo personaje, y la Máquina 2 arranca sabiendo lo que ya preguntó la 1.
 */
public class Partida {

    public enum Fin { SIGUE, PASA_A_RONDA_2, VICTORIA_TOTAL, DERROTA }

    /** Qué pasó tras una acción: líneas para el registro, cómo sigue y un resumen si terminó algo. */
    public record Turno(List<String> mensajes, Fin fin, String resumen) {
    }

    private final GuardarMarcadorTXT marcador;
    private final List<Personaje> personajes;
    private final String nombreUsuario;
    private final Personaje secretoHumano;
    private final List<PreguntaRespuesta> historialMaquina1 = new ArrayList<>();

    private MaquinaJugadora maquina;
    private List<Personaje> candidatosHumano;
    private int ronda = 1;
    private int turno = 1;
    private int turnosRonda1;

    public Partida(String archivoMarcador, List<Personaje> personajes, String nombreUsuario, Personaje secretoHumano) {
        this.marcador = new GuardarMarcadorTXT(archivoMarcador);
        this.personajes = personajes;
        this.nombreUsuario = nombreUsuario;
        this.secretoHumano = secretoHumano;

        // La máquina elige entre los 23: puede tocarle el mismo personaje que al jugador.
        this.maquina = new MaquinaBasica(personajes);
        this.maquina.elegirSecreto(null);
        reiniciarCandidatos();
    }

    // ---------------------------------------------------------------
    // Consultas para la ventana
    // ---------------------------------------------------------------

    public int getRonda() {
        return ronda;
    }

    public int getTurno() {
        return turno;
    }

    public String getNombreMaquina() {
        return maquina.getNombre();
    }

    /** Los personajes que todavía podrían ser el secreto de la máquina (según lo que ya preguntó el jugador). */
    public List<Personaje> getCandidatosHumano() {
        return Collections.unmodifiableList(candidatosHumano);
    }

    public List<String> mensajesDeInicio() {
        List<String> mensajes = new ArrayList<>();
        mensajes.add("=== Ronda 1: contra " + maquina.getNombre() + " ===");
        mensajes.add(maquina.getNombre() + " ya eligió su personaje en secreto. ¡Empieza el duelo!");
        mensajes.add("Ojo: si arriesgás una adivinanza directa y le errás, perdés el desafío en el acto.");
        return mensajes;
    }

    // ---------------------------------------------------------------
    // Acciones del jugador
    // ---------------------------------------------------------------

    public Turno preguntar(Pregunta pregunta) {
        List<String> mensajes = new ArrayList<>();
        boolean respuesta = maquina.responder(pregunta);
        mensajes.add("Le preguntás a " + maquina.getNombre() + ": ¿su personaje " + pregunta.getClausula()
                + "? → " + (respuesta ? "Sí" : "No"));
        candidatosHumano.removeIf(p -> pregunta.evaluar(p) != respuesta);
        mensajes.add("Con esa pista, te quedan " + candidatosHumano.size() + " personajes posibles.");
        return turnoDeLaMaquina(mensajes);
    }

    public Turno adivinar(int idPersonaje) {
        List<String> mensajes = new ArrayList<>();
        if (maquina.getSecreto().getId() == idPersonaje) {
            mensajes.add("¡Adivinaste! Era " + maquina.getSecreto().getNombre() + ".");
            return ganoLaRonda(mensajes);
        }
        mensajes.add("No, no es ese personaje.");
        return perdio(mensajes, "Arriesgaste una adivinanza y le erraste: ¡perdiste el desafío de una! El personaje de "
                + maquina.getNombre() + " era " + maquina.getSecreto().getNombre() + ".");
    }

    public Turno rendirse() {
        return perdio(new ArrayList<>(), "Te rendiste. El personaje de " + maquina.getNombre()
                + " era " + maquina.getSecreto().getNombre() + ".");
    }

    // ---------------------------------------------------------------
    // Turno de la máquina
    // ---------------------------------------------------------------

    private Turno turnoDeLaMaquina(List<String> mensajes) {
        mensajes.add(maquina.getNombre() + " está pensando...");

        MaquinaJugadora.Jugada jugada = maquina.decidirJugada();
        if (jugada.esAdivinanza()) {
            return arriesgarLaMaquina(mensajes, jugada);
        }

        Pregunta pregunta = jugada.pregunta();
        boolean respuesta = pregunta.evaluar(secretoHumano);
        mensajes.add(maquina.getNombre() + " pregunta: " + pregunta.getDescripcion() + " → " + (respuesta ? "Sí" : "No"));
        maquina.registrarRespuesta(pregunta, respuesta);
        if (ronda == 1) {
            historialMaquina1.add(new PreguntaRespuesta(pregunta, respuesta));
        }
        turno++;
        return new Turno(mensajes, Fin.SIGUE, null);
    }

    private Turno arriesgarLaMaquina(List<String> mensajes, MaquinaJugadora.Jugada jugada) {
        Personaje adivinanza = jugada.adivinanza();
        String frase = jugada.sinPreguntasUtiles()
                ? " ya no tiene más preguntas útiles y arriesga: ¿sos "
                : " arriesga: ¿sos ";
        mensajes.add(maquina.getNombre() + frase + adivinanza.getNombre() + "?");
        if (adivinanza.getId() == secretoHumano.getId()) {
            return perdio(mensajes, maquina.getNombre() + " adivinó tu personaje ("
                    + secretoHumano.getNombre() + "). ¡Perdiste el desafío!");
        }
        mensajes.add("No, falló su intento. ¡Eso le hace perder el desafío a " + maquina.getNombre() + "!");
        return ganoLaRonda(mensajes);
    }

    // ---------------------------------------------------------------
    // Fin de ronda
    // ---------------------------------------------------------------

    private Turno ganoLaRonda(List<String> mensajes) {
        if (ronda == 1) {
            turnosRonda1 = turno;
            marcador.sumarVictoriaMaquina1(nombreUsuario);
            String resumen = "¡Le ganaste a " + maquina.getNombre() + " en " + turno + " intento(s)!";
            mensajes.add(resumen);

            MaquinaAvanzada maquina2 = new MaquinaAvanzada(personajes);
            // Cada máquina elige un personaje distinto del de la otra (pero puede coincidir con el del jugador).
            maquina2.elegirSecreto(List.of(maquina.getSecreto()));
            for (PreguntaRespuesta pr : historialMaquina1) {
                maquina2.registrarRespuesta(pr.pregunta(), pr.respuesta());
            }

            String aviso = maquina2.getNombre() + " ya escuchó las " + historialMaquina1.size()
                    + " pregunta(s) que te hizo " + maquina.getNombre() + ", así que arranca sabiendo eso y no las repite.";
            mensajes.add("=== Ronda 2: contra " + maquina2.getNombre() + " ===");
            mensajes.add(aviso);

            maquina = maquina2;
            ronda = 2;
            turno = 1;
            reiniciarCandidatos();
            return new Turno(mensajes, Fin.PASA_A_RONDA_2,
                    resumen + "\n\nAhora te enfrentás a " + maquina2.getNombre() + ".\n" + aviso);
        }

        int intentosTotales = turnosRonda1 + turno;
        marcador.sumarVictoriaCompleta(nombreUsuario, intentosTotales);
        String resumen = "¡Ganaste el desafío completo, " + nombreUsuario + "! En total te llevó "
                + intentosTotales + " intento(s).";
        mensajes.add(resumen);
        return new Turno(mensajes, Fin.VICTORIA_TOTAL, resumen);
    }

    private Turno perdio(List<String> mensajes, String resumen) {
        marcador.sumarDerrota(nombreUsuario);
        mensajes.add(resumen);
        return new Turno(mensajes, Fin.DERROTA, resumen);
    }

    /** Al empezar cada ronda, cualquiera de los 23 puede ser el de la máquina. */
    private void reiniciarCandidatos() {
        candidatosHumano = new ArrayList<>(personajes);
    }
}
