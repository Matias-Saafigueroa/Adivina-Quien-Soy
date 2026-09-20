package Servicios;

import Modelos.Personaje;

import java.util.ArrayList;
import java.util.List;

/**
 * Modo espectador (Máquina 1 contra Máquina 2) para la interfaz Swing: cada llamada
 * a siguientePaso() hace jugar a una sola máquina y devuelve las líneas para mostrar.
 * Las dos máquinas juegan por turnos; ninguna conoce el personaje de la otra.
 */
public class Espectador {

    private final MaquinaBasica maquina1;
    private final MaquinaAvanzada maquina2;
    private int turno = 1;
    private boolean juegaMaquina1 = true;
    private boolean terminada = false;
    private String ganador;

    public Espectador(List<Personaje> personajes) {
        maquina1 = new MaquinaBasica(personajes);
        maquina2 = new MaquinaAvanzada(personajes);
        maquina1.elegirSecreto(null);
        maquina2.elegirSecreto(List.of(maquina1.getSecreto()));
    }

    public boolean isTerminada() {
        return terminada;
    }

    /** Nombre de la máquina que ganó (solo tiene sentido cuando la partida terminó). */
    public String getGanador() {
        return ganador;
    }

    // Consultas para que la ventana dibuje el estado de cada máquina.
    // Los candidatos de una máquina son los personajes que todavía cree posibles para el secreto de su rival.

    public String getNombreMaquina1() {
        return maquina1.getNombre();
    }

    public String getNombreMaquina2() {
        return maquina2.getNombre();
    }

    public Personaje getSecretoMaquina1() {
        return maquina1.getSecreto();
    }

    public Personaje getSecretoMaquina2() {
        return maquina2.getSecreto();
    }

    public List<Personaje> getCandidatosMaquina1() {
        return maquina1.getCandidatos();
    }

    public List<Personaje> getCandidatosMaquina2() {
        return maquina2.getCandidatos();
    }

    public List<String> mensajesDeInicio() {
        List<String> mensajes = new ArrayList<>();
        mensajes.add("=== Modo espectador: Máquina vs Máquina ===");
        mensajes.add("Acá las dos parten en igualdad de condiciones (cada una busca un personaje distinto),");
        mensajes.add("para poder comparar sus dos formas de jugar.");
        mensajes.add("(Secreto de " + maquina1.getNombre() + ": " + maquina1.getSecreto().getNombre() + " — oculto para su rival)");
        mensajes.add("(Secreto de " + maquina2.getNombre() + ": " + maquina2.getSecreto().getNombre() + " — oculto para su rival)");
        return mensajes;
    }

    /** Hace jugar a la máquina que le toca y devuelve lo que hizo. */
    public List<String> siguientePaso() {
        List<String> mensajes = new ArrayList<>();
        if (terminada) return mensajes;

        MaquinaJugadora activo = juegaMaquina1 ? maquina1 : maquina2;
        MaquinaJugadora rival = juegaMaquina1 ? maquina2 : maquina1;

        if (juegaMaquina1) {
            mensajes.add("");
            mensajes.add("--- Turno " + turno + " ---");
        }
        terminada = jugar(activo, rival, mensajes);

        if (!juegaMaquina1) turno++;
        juegaMaquina1 = !juegaMaquina1;
        return mensajes;
    }

    /** @return true si la partida terminó en este paso (por acierto o por un intento fallido). */
    private boolean jugar(MaquinaJugadora activo, MaquinaJugadora rival, List<String> mensajes) {
        mensajes.add(activo.getNombre() + " le quedan " + activo.getCandidatos().size()
                + " candidato(s) para el secreto de " + rival.getNombre() + ".");

        MaquinaJugadora.Jugada jugada = activo.decidirJugada();
        if (jugada.esAdivinanza()) {
            return arriesgar(activo, rival, mensajes, jugada);
        }

        Pregunta pregunta = jugada.pregunta();
        boolean respuesta = rival.responder(pregunta);
        mensajes.add(activo.getNombre() + " pregunta a " + rival.getNombre() + ": " + pregunta.getDescripcion()
                + " → " + (respuesta ? "Sí" : "No"));
        activo.registrarRespuesta(pregunta, respuesta);
        return false;
    }

    private boolean arriesgar(MaquinaJugadora activo, MaquinaJugadora rival, List<String> mensajes,
                              MaquinaJugadora.Jugada jugada) {
        Personaje adivinanza = jugada.adivinanza();
        String frase = jugada.sinPreguntasUtiles()
                ? " ya no tiene preguntas útiles y arriesga: ¿es "
                : " arriesga: ¿es ";
        mensajes.add(activo.getNombre() + frase + adivinanza.getNombre() + "?");
        if (rival.esMiPersonaje(adivinanza)) {
            ganador = activo.getNombre();
            mensajes.add("¡Correcto! " + activo.getNombre() + " gana la partida.");
        } else {
            ganador = rival.getNombre();
            mensajes.add("Falló. No era " + adivinanza.getNombre() + ". ¡Eso le hace perder la partida a "
                    + activo.getNombre() + "! Gana " + rival.getNombre() + ".");
        }
        return true;
    }
}
