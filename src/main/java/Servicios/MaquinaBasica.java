package Servicios;

import DataSystem.GuardarPreguntasMaquinaTXT;
import Modelos.Personaje;

import java.util.List;

/**
 * Máquina 1: juega de forma intuitiva. Elige preguntas al azar entre las
 * disponibles y a veces arriesga una adivinanza antes de estar segura, por
 * lo que es la menos acertiva de las dos. Cada pregunta que formula queda
 * registrada en un log para que la Máquina 2 pueda aprovecharlo.
 */
public class MaquinaBasica extends MaquinaJugadora {

    private final GuardarPreguntasMaquinaTXT log;

    public MaquinaBasica(List<Personaje> personajesBase, String archivoLog) {
        super("Máquina 1 (intuitiva)", personajesBase);
        this.log = new GuardarPreguntasMaquinaTXT(archivoLog);
    }

    @Override
    public boolean debeArriesgar() {
        if (candidatos.size() <= 2) return true;
        // A veces se apura a adivinar aunque le queden varios candidatos.
        return candidatos.size() <= 4 && random.nextInt(4) == 0;
    }

    @Override
    public Pregunta elegirPregunta() {
        List<Pregunta> disponibles = preguntasDisponibles();
        if (disponibles.isEmpty()) return null;
        Pregunta elegida = disponibles.get(random.nextInt(disponibles.size()));
        log.registrar(elegida.getClave());
        return elegida;
    }

    @Override
    public Personaje elegirAdivinanza() {
        if (candidatos.isEmpty()) return personajesBase.get(random.nextInt(personajesBase.size()));
        return candidatos.get(random.nextInt(candidatos.size()));
    }
}
