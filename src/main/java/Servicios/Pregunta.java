package Servicios;

import Modelos.ColorPelo;
import Modelos.Descriptible;
import Modelos.Edad;
import Modelos.Filtro;
import Modelos.Genero;
import Modelos.Grupo;
import Modelos.Personaje;

/**
 * Una pregunta-filtro: "¿el personaje cumple <filtro> = <valor>?".
 * Es la única forma en la que un jugador puede preguntar sobre el personaje
 * secreto del rival: nunca se expone el objeto Personaje elegido, sólo
 * respuestas booleanas a preguntas como estas calvo o no.
 */
public class Pregunta {

    private final Filtro filtro;
    private final Object valor;

    public Pregunta(Filtro filtro, Object valor) {
        this.filtro = filtro;
        this.valor = valor;
    }

    public boolean evaluar(Personaje p) {
        return switch (filtro) {
            case GENERO -> p.getGenero() == (Genero) valor;
            case CALVICIE -> p.isCalvo() == (Boolean) valor;
            case LENTES -> p.isLentes() == (Boolean) valor;
            case COLOR_PELO -> p.getColorPelo() == (ColorPelo) valor;
            case EDAD -> p.getEdad() == (Edad) valor;
            case GRUPO -> p.getGrupo() == (Grupo) valor;
        };
    }

    public Filtro getFiltro() {
        return filtro;
    }

    public Object getValor() {
        return valor;
    }

    public String getDescripcion() {
        return "¿Tu personaje " + getClausula() + "?";
    }

    public String getClausula() {
        return switch (filtro) {
            case GENERO -> "es de género " + valor;
            case CALVICIE -> (Boolean) valor ? "es calvo" : "no es calvo";
            case LENTES -> (Boolean) valor ? "usa lentes" : "no usa lentes";
            case COLOR_PELO -> "tiene el pelo " + valor;
            case EDAD, GRUPO -> ((Descriptible) valor).clausula();
        };
    }


    public String getClave() {
        return filtro + "=" + valor;
    }

    @Override
    public String toString() {
        return getDescripcion();
    }
}