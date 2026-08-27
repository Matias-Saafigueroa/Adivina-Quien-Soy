package Modelos;

/**
 * Registro acumulado de un usuario en el marcador: cuántas partidas ganó,
 * cuántas perdió (incluye rendiciones), y en la menor cantidad de intentos
 * que logró ganar.
 */
public class RegistroMarcador {

    private int victorias;
    private int derrotas;
    private int mejorIntentos;

    public RegistroMarcador(int victorias, int derrotas, int mejorIntentos) {
        this.victorias = victorias;
        this.derrotas = derrotas;
        this.mejorIntentos = mejorIntentos;
    }

    public static RegistroMarcador vacio() {
        return new RegistroMarcador(0, 0, Integer.MAX_VALUE);
    }

    public void registrarVictoria(int intentos) {
        victorias++;
        if (intentos < mejorIntentos) {
            mejorIntentos = intentos;
        }
    }

    public void registrarDerrota() {
        derrotas++;
    }

    public int getVictorias() {
        return victorias;
    }

    public int getDerrotas() {
        return derrotas;
    }

    public int getMejorIntentos() {
        return mejorIntentos;
    }

    public boolean tieneVictorias() {
        return victorias > 0 && mejorIntentos != Integer.MAX_VALUE;
    }
}
