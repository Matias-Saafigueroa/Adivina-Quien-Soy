package Modelos;

/**
 * Registro acumulado de un jugador en el marcador: cuántas veces le ganó a
 * Máquina 1, cuántas veces completó el desafío ganándoles a las dos,
 * cuántas partidas perdió (incluye rendiciones), y en la menor cantidad de
 * intentos que logró completar el desafío.
 */
public class RegistroMarcador {

    private int victoriasMaquina1;
    private int victoriasCompletas;
    private int derrotas;
    private int mejorIntentos;

    public RegistroMarcador(int victoriasMaquina1, int victoriasCompletas, int derrotas, int mejorIntentos) {
        this.victoriasMaquina1 = victoriasMaquina1;
        this.victoriasCompletas = victoriasCompletas;
        this.derrotas = derrotas;
        this.mejorIntentos = mejorIntentos;
    }

    public static RegistroMarcador vacio() {
        return new RegistroMarcador(0, 0, 0, Integer.MAX_VALUE);
    }

    public void registrarVictoriaMaquina1() {
        victoriasMaquina1++;
    }

    public void registrarVictoriaCompleta(int intentos) {
        victoriasCompletas++;
        if (intentos < mejorIntentos) {
            mejorIntentos = intentos;
        }
    }

    public void registrarDerrota() {
        derrotas++;
    }

    public int getVictoriasMaquina1() {
        return victoriasMaquina1;
    }

    public int getVictoriasCompletas() {
        return victoriasCompletas;
    }

    public int getDerrotas() {
        return derrotas;
    }

    public int getMejorIntentos() {
        return mejorIntentos;
    }

    public boolean tieneVictoriasCompletas() {
        return victoriasCompletas > 0 && mejorIntentos != Integer.MAX_VALUE;
    }
}
