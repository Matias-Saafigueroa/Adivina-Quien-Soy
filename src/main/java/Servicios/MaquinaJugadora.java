package Servicios;

import Modelos.ColorPelo;
import Modelos.Filtro;
import Modelos.Genero;
import Modelos.Personaje;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Base común de los jugadores automáticos. Guarda su propio personaje secreto
 * (nunca accesible desde afuera) y su lista de candidatos para el personaje
 * del rival, que va acortando a medida que recibe respuestas.
 */
public abstract class MaquinaJugadora {

    protected final String nombre;
    protected final List<Personaje> personajesBase;
    protected final Random random = new Random();
    protected final Set<String> preguntasHechas = new HashSet<>();

    protected List<Personaje> candidatos;
    private Personaje secreto;

    protected MaquinaJugadora(String nombre, List<Personaje> personajesBase) {
        this.nombre = nombre;
        this.personajesBase = personajesBase;
    }

    public String getNombre() {
        return nombre;
    }

    /** Elige su propio personaje al azar, evitando repetir el del rival. */
    public void elegirSecreto(Personaje distintoDe) {
        List<Personaje> opciones = new ArrayList<>(personajesBase);
        if (distintoDe != null) {
            opciones.removeIf(p -> p.getId() == distintoDe.getId());
        }
        this.secreto = opciones.get(random.nextInt(opciones.size()));
        this.candidatos = new ArrayList<>(personajesBase);
        this.preguntasHechas.clear();
    }

    /** Responde con la verdad sobre su propio personaje secreto, sin exponerlo. */
    public boolean responder(Pregunta pregunta) {
        return pregunta.evaluar(secreto);
    }

    public boolean esMiPersonaje(Personaje candidato) {
        return secreto.getId() == candidato.getId();
    }

    public Personaje getSecreto() {
        return secreto;
    }

    public List<Personaje> getCandidatos() {
        return candidatos;
    }

    public void registrarRespuesta(Pregunta pregunta, boolean respuesta) {
        candidatos.removeIf(p -> pregunta.evaluar(p) != respuesta);
        preguntasHechas.add(pregunta.getClave());
    }

    protected List<Pregunta> preguntasDisponibles() {
        List<Pregunta> todas = new ArrayList<>();
        for (Genero g : Genero.values()) todas.add(new Pregunta(Filtro.GENERO, g));
        todas.add(new Pregunta(Filtro.CALVICIE, Boolean.TRUE));
        todas.add(new Pregunta(Filtro.CALVICIE, Boolean.FALSE));
        todas.add(new Pregunta(Filtro.LENTES, Boolean.TRUE));
        todas.add(new Pregunta(Filtro.LENTES, Boolean.FALSE));
        for (ColorPelo c : ColorPelo.values()) todas.add(new Pregunta(Filtro.COLOR_PELO, c));
        todas.removeIf(p -> preguntasHechas.contains(p.getClave()));
        return todas;
    }

    /** true si en este turno conviene arriesgar una adivinanza directa. */
    public abstract boolean debeArriesgar();

    /** Siguiente pregunta-filtro a formular sobre el personaje del rival. */
    public abstract Pregunta elegirPregunta();

    /** Personaje que arriesga como adivinanza entre los candidatos actuales. */
    public abstract Personaje elegirAdivinanza();
}
