package Servicios;

import Modelos.Personaje;

import java.util.List;

/**
 * Máquina 2: juega de forma analítica. En cada turno elige la pregunta que
 * divide más parejo a los candidatos restantes (máxima reducción del
 * espacio de búsqueda en el peor caso), y sólo arriesga una adivinanza
 * cuando ya está segura o no le quedan preguntas útiles.
 *
 * Su ventaja frente a Máquina 1 no es sólo este mejor criterio de selección:
 * cuando compite contra el jugador, arranca heredando las preguntas y
 * respuestas que Máquina 1 ya obtuvo sobre ese mismo personaje,
 * así que nunca repite una pregunta ya hecha y empieza
 * con el espacio de búsqueda ya recortado.
 */
public class MaquinaAvanzada extends MaquinaJugadora {

    public MaquinaAvanzada(List<Personaje> personajesBase) {
        super("Máquina 2 (analítica)", personajesBase);
    }

    @Override
    public boolean debeArriesgar() {
        return candidatos.size() <= 1 || preguntasDisponibles().isEmpty();
    }

    @Override
    public Pregunta elegirPregunta() {
        List<Pregunta> disponibles = preguntasDisponibles();
        if (disponibles.isEmpty()) return null;

        Pregunta mejor = null;
        int mejorPuntaje = -1;

        for (Pregunta p : disponibles) {
            int siCount = 0;
            for (Personaje c : candidatos) {
                if (p.evaluar(c)) siCount++;
            }
            int noCount = candidatos.size() - siCount;
            int puntaje = Math.min(siCount, noCount);

            if (puntaje > mejorPuntaje) {
                mejor = p;
                mejorPuntaje = puntaje;
            }
        }
        return mejor;
    }

    @Override
    public Personaje elegirAdivinanza() {
        if (candidatos.isEmpty()) return personajesBase.get(random.nextInt(personajesBase.size()));
        return candidatos.get(random.nextInt(candidatos.size()));
    }
}
