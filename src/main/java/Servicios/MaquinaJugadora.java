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

    /** Elige su propio personaje al azar, evitando repetir alguno de los ya elegidos por otros jugadores. */
    public void elegirSecreto(List<Personaje> excluir) {
        List<Personaje> opciones = new ArrayList<>(personajesBase);
        if (excluir != null) {
            for (Personaje p : excluir) {
                opciones.removeIf(o -> o.getId() == p.getId());
            }
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

    /**
     * Preguntas que todavía aportan información nueva. Si de un filtro con N
     * valores posibles ya se preguntaron N-1, el valor que falta ya se puede
     * deducir sin gastar un turno, así que se lo excluye también.
     */
    protected List<Pregunta> preguntasDisponibles() {
        List<Pregunta> todas = new ArrayList<>();
        agregarSiAportaInfo(todas, Filtro.GENERO, List.of(Genero.MASCULINO, Genero.FEMENINO));
        agregarSiAportaInfo(todas, Filtro.CALVICIE, List.of(Boolean.TRUE, Boolean.FALSE));
        agregarSiAportaInfo(todas, Filtro.LENTES, List.of(Boolean.TRUE, Boolean.FALSE));
        agregarSiAportaInfo(todas, Filtro.COLOR_PELO, List.of(ColorPelo.COLORADO, ColorPelo.NEGRO, ColorPelo.AMARILLO));
        return todas;
    }

    private void agregarSiAportaInfo(List<Pregunta> destino, Filtro filtro, List<Object> valoresPosibles) {
        long yaPreguntados = valoresPosibles.stream()
                .filter(v -> preguntasHechas.contains(filtro + "=" + v))
                .count();
        if (yaPreguntados >= valoresPosibles.size() - 1) {
            return;
        }
        for (Object valor : valoresPosibles) {
            if (!preguntasHechas.contains(filtro + "=" + valor)) {
                destino.add(new Pregunta(filtro, valor));
            }
        }
    }

    /** true si en este turno conviene arriesgar una adivinanza directa. */
    public abstract boolean debeArriesgar();

    /** Siguiente pregunta-filtro a formular sobre el personaje del rival. */
    public abstract Pregunta elegirPregunta();

    /** Personaje que arriesga como adivinanza entre los candidatos actuales. */
    public abstract Personaje elegirAdivinanza();
}
