package Servicios;

import DataSystem.GuardarPreguntasMaquinaTXT;
import Modelos.Personaje;

import java.util.List;
import java.util.Set;

/**
 * Máquina 2: juega de forma analítica. En cada turno elige la pregunta que
 * divide más parejo a los candidatos restantes (máxima reducción del
 * espacio de búsqueda), y sólo arriesga una adivinanza cuando está segura.
 * Además parte con la ventaja de conocer, desde el historial persistido,
 * qué preguntas usó la Máquina 1: ante un empate de utilidad prioriza esas.
 */
public class MaquinaAvanzada extends MaquinaJugadora {

    private final Set<String> preguntasConocidasDeMaquina1;

    public MaquinaAvanzada(List<Personaje> personajesBase, String archivoLogMaquina1) {
        super("Máquina 2 (analítica)", personajesBase);
        this.preguntasConocidasDeMaquina1 = new GuardarPreguntasMaquinaTXT(archivoLogMaquina1).leerClaves();
    }

    public int cantidadPreguntasConocidas() {
        return preguntasConocidasDeMaquina1.size();
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
        boolean mejorEsConocida = false;

        for (Pregunta p : disponibles) {
            int siCount = 0;
            for (Personaje c : candidatos) {
                if (p.evaluar(c)) siCount++;
            }
            int noCount = candidatos.size() - siCount;
            int puntaje = Math.min(siCount, noCount);
            boolean esConocida = preguntasConocidasDeMaquina1.contains(p.getClave());

            boolean mejora = puntaje > mejorPuntaje || (puntaje == mejorPuntaje && esConocida && !mejorEsConocida);
            if (mejora) {
                mejor = p;
                mejorPuntaje = puntaje;
                mejorEsConocida = esConocida;
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
