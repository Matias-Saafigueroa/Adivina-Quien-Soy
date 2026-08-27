package Servicios;

import DataSystem.GuardarMarcadorTXT;
import DataSystem.RepositorioPersonajes;
import Modelos.ColorPelo;
import Modelos.Filtro;
import Modelos.Genero;
import Modelos.Personaje;
import Modelos.RegistroMarcador;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Motor del juego. Coordina las tres modalidades: humano contra una de las
 * dos máquinas, y máquina contra máquina en modo espectador.
 *
 * El personaje elegido por el humano se guarda en una variable local de este
 * motor: las máquinas nunca reciben una referencia a él, sólo pueden
 * consultarlo indirectamente a través de {@link Pregunta#evaluar}.
 */
public class Juego {

    private final RepositorioPersonajes repositorioPersonajes;
    private final GuardarMarcadorTXT marcador;
    private final String archivoLogMaquina1;
    private final Scanner teclado;

    public Juego(String archivoPersonajes, String archivoMarcador, String archivoLogMaquina1, Scanner teclado) {
        this.repositorioPersonajes = new RepositorioPersonajes(archivoPersonajes);
        this.marcador = new GuardarMarcadorTXT(archivoMarcador);
        this.archivoLogMaquina1 = archivoLogMaquina1;
        this.teclado = teclado;
    }

    // ---------------------------------------------------------------
    // Humano vs Máquina
    // ---------------------------------------------------------------

    public void jugarContraMaquina(String nombreUsuario, MaquinaJugadora maquina) {
        List<Personaje> personajes = repositorioPersonajes.cargar();

        Personaje secretoHumano = elegirPersonajeHumano(personajes);
        maquina.elegirSecreto(secretoHumano);

        System.out.println("\n" + maquina.getNombre() + " ya eligió su personaje en secreto. ¡Empieza el duelo!");

        Set<String> preguntasHumanoHechas = new HashSet<>();
        List<Personaje> candidatosHumano = new ArrayList<>(personajes);
        candidatosHumano.removeIf(p -> p.getId() == secretoHumano.getId());

        int turno = 1;
        while (true) {
            System.out.println("\n--- Turno " + turno + " ---");

            ResultadoTurnoHumano resultado = turnoHumano(secretoHumano, maquina, candidatosHumano, preguntasHumanoHechas);
            if (resultado == ResultadoTurnoHumano.GANO) {
                System.out.println("\n¡Ganaste, " + nombreUsuario + "! Adivinaste que era " + maquina.getSecreto().getNombre()
                        + " en " + turno + " intento(s).");
                marcador.sumarVictoria(nombreUsuario, turno);
                break;
            }
            if (resultado == ResultadoTurnoHumano.RINDIO) {
                System.out.println("\nTe rendiste. El personaje de " + maquina.getNombre() + " era " + maquina.getSecreto().getNombre() + ".");
                marcador.sumarDerrota(nombreUsuario);
                break;
            }

            if (turnoMaquina(maquina, secretoHumano)) {
                System.out.println("\n" + maquina.getNombre() + " adivinó tu personaje (" + secretoHumano.getNombre() + "). ¡Perdiste esta partida!");
                marcador.sumarDerrota(nombreUsuario);
                break;
            }

            turno++;
        }

        mostrarMarcador();
    }

    private enum ResultadoTurnoHumano { GANO, RINDIO, SIGUE }

    private Personaje elegirPersonajeHumano(List<Personaje> personajes) {
        Personaje elegido = null;
        while (elegido == null) {
            mostrarPersonajes(personajes);
            System.out.print("Elegí tu personaje secreto por id (no vas a poder cambiarlo): ");
            int id = leerEntero();
            elegido = buscarPorId(personajes, id);
            if (elegido == null) {
                System.out.println("Ese id no existe, probá de nuevo.");
            }
        }
        System.out.println("Tu personaje secreto es " + elegido.getNombre() + ". ¡Que no se entere la máquina!");
        return elegido;
    }

    private ResultadoTurnoHumano turnoHumano(Personaje secretoHumano, MaquinaJugadora maquina,
                                              List<Personaje> candidatosHumano, Set<String> preguntasHechas) {
        System.out.println("\nTu turno. Personajes que todavía podrían ser el secreto de " + maquina.getNombre() + ":");
        mostrarPersonajes(candidatosHumano);
        System.out.println("(" + candidatosHumano.size() + " personaje(s) posible(s))");
        System.out.println("1) Preguntar por un filtro   2) Adivinar directamente   3) Rendirme");
        System.out.print("Elegí una opción: ");
        int opcion = leerEntero();

        if (opcion == 2) {
            System.out.print("¿Quién creés que es? Ingresá el id: ");
            int idAdivinado = leerEntero();
            if (maquina.getSecreto().getId() == idAdivinado) {
                return ResultadoTurnoHumano.GANO;
            }
            System.out.println("No, no es ese personaje.");
            candidatosHumano.removeIf(p -> p.getId() == idAdivinado);
            return ResultadoTurnoHumano.SIGUE;
        }

        if (opcion == 3) {
            return ResultadoTurnoHumano.RINDIO;
        }

        Pregunta pregunta = pedirPreguntaAlHumano(preguntasHechas);
        boolean respuesta = maquina.responder(pregunta);
        System.out.println("Le preguntás a " + maquina.getNombre() + ": ¿su personaje " + pregunta.getClausula() + "? → " + (respuesta ? "Sí" : "No"));
        preguntasHechas.add(pregunta.getClave());
        candidatosHumano.removeIf(p -> pregunta.evaluar(p) != respuesta);
        System.out.println("Con esa pista, te quedan " + candidatosHumano.size() + " personajes posibles.");
        return ResultadoTurnoHumano.SIGUE;
    }

    /** @return true si la máquina adivinó el personaje del humano. */
    private boolean turnoMaquina(MaquinaJugadora maquina, Personaje secretoHumano) {
        System.out.println(maquina.getNombre() + " está pensando...");

        if (maquina.debeArriesgar()) {
            Personaje adivinanza = maquina.elegirAdivinanza();
            System.out.println(maquina.getNombre() + " arriesga: ¿sos " + adivinanza.getNombre() + "?");
            if (adivinanza.getId() == secretoHumano.getId()) {
                return true;
            }
            System.out.println("No, falló su intento.");
            maquina.getCandidatos().removeIf(p -> p.getId() == adivinanza.getId());
            return false;
        }

        Pregunta pregunta = maquina.elegirPregunta();
        if (pregunta == null) {
            Personaje adivinanza = maquina.elegirAdivinanza();
            System.out.println(maquina.getNombre() + " ya no tiene más preguntas útiles y arriesga: ¿sos " + adivinanza.getNombre() + "?");
            if (adivinanza.getId() == secretoHumano.getId()) {
                return true;
            }
            System.out.println("No, falló su intento.");
            maquina.getCandidatos().removeIf(p -> p.getId() == adivinanza.getId());
            return false;
        }

        boolean respuesta = pregunta.evaluar(secretoHumano);
        System.out.println(maquina.getNombre() + " pregunta: " + pregunta.getDescripcion() + " → " + (respuesta ? "Sí" : "No"));
        maquina.registrarRespuesta(pregunta, respuesta);
        return false;
    }

    private Pregunta pedirPreguntaAlHumano(Set<String> preguntasHechas) {
        while (true) {
            System.out.println("Filtros: 1) Género  2) Calvicie  3) Lentes  4) Color de pelo");
            System.out.print("Elegí sobre qué filtro querés preguntarle a la máquina: ");
            int filtroOpcion = leerEntero();

            Pregunta pregunta = switch (filtroOpcion) {
                case 1 -> new Pregunta(Filtro.GENERO, pedirValorGenero());
                case 2 -> new Pregunta(Filtro.CALVICIE, Boolean.TRUE);
                case 3 -> new Pregunta(Filtro.LENTES, Boolean.TRUE);
                case 4 -> new Pregunta(Filtro.COLOR_PELO, pedirValorColorPelo());
                default -> null;
            };

            if (pregunta == null) {
                System.out.println("Opción inválida.");
                continue;
            }
            if (preguntasHechas.contains(pregunta.getClave())) {
                System.out.println("Ya preguntaste eso, probá con otro filtro.");
                continue;
            }
            return pregunta;
        }
    }

    private Genero pedirValorGenero() {
        System.out.print("¿Sobre qué género querés preguntar? (m = masculino / f = femenino): ");
        String r = teclado.next().trim().toLowerCase();
        return r.startsWith("m") ? Genero.MASCULINO : Genero.FEMENINO;
    }

    private ColorPelo pedirValorColorPelo() {
        System.out.print("¿Sobre qué color de pelo querés preguntar? (c = colorado / n = negro / a = amarillo): ");
        String r = teclado.next().trim().toLowerCase();
        return switch (r.charAt(0)) {
            case 'n' -> ColorPelo.NEGRO;
            case 'a' -> ColorPelo.AMARILLO;
            default -> ColorPelo.COLORADO;
        };
    }

    // ---------------------------------------------------------------
    // Máquina vs Máquina (espectador)
    // ---------------------------------------------------------------

    public void jugarMaquinaVsMaquina() {
        List<Personaje> personajes = repositorioPersonajes.cargar();

        MaquinaBasica maquina1 = new MaquinaBasica(personajes, archivoLogMaquina1);
        MaquinaAvanzada maquina2 = new MaquinaAvanzada(personajes, archivoLogMaquina1);

        maquina1.elegirSecreto(null);
        maquina2.elegirSecreto(maquina1.getSecreto());

        System.out.println("\n=== Modo espectador: Máquina vs Máquina ===");
        System.out.println(maquina2.getNombre() + " arranca conociendo " + maquina2.cantidadPreguntasConocidas()
                + " pregunta(s) del historial de " + maquina1.getNombre() + ".");
        System.out.println("(Secreto de " + maquina1.getNombre() + ": " + maquina1.getSecreto().getNombre() + " — oculto para su rival)");
        System.out.println("(Secreto de " + maquina2.getNombre() + ": " + maquina2.getSecreto().getNombre() + " — oculto para su rival)");

        int turno = 1;
        while (true) {
            System.out.println("\n--- Turno " + turno + " ---");
            pausa();
            if (turnoEspectador(maquina1, maquina2)) {
                break;
            }
            pausa();
            if (turnoEspectador(maquina2, maquina1)) {
                break;
            }
            turno++;
        }
    }

    /** @return true si el jugador activo adivinó y terminó la partida. */
    private boolean turnoEspectador(MaquinaJugadora activo, MaquinaJugadora rival) {
        System.out.println(activo.getNombre() + " le quedan " + activo.getCandidatos().size() + " candidato(s) para el secreto de " + rival.getNombre() + ".");

        if (activo.debeArriesgar()) {
            Personaje adivinanza = activo.elegirAdivinanza();
            System.out.println(activo.getNombre() + " arriesga: ¿es " + adivinanza.getNombre() + "?");
            if (rival.esMiPersonaje(adivinanza)) {
                System.out.println("¡Correcto! " + activo.getNombre() + " gana la partida.");
                return true;
            }
            System.out.println("Falló. No era " + adivinanza.getNombre() + ".");
            activo.getCandidatos().removeIf(p -> p.getId() == adivinanza.getId());
            return false;
        }

        Pregunta pregunta = activo.elegirPregunta();
        if (pregunta == null) {
            Personaje adivinanza = activo.elegirAdivinanza();
            System.out.println(activo.getNombre() + " ya no tiene preguntas útiles y arriesga: ¿es " + adivinanza.getNombre() + "?");
            if (rival.esMiPersonaje(adivinanza)) {
                System.out.println("¡Correcto! " + activo.getNombre() + " gana la partida.");
                return true;
            }
            System.out.println("Falló.");
            activo.getCandidatos().removeIf(p -> p.getId() == adivinanza.getId());
            return false;
        }

        boolean respuesta = rival.responder(pregunta);
        System.out.println(activo.getNombre() + " pregunta a " + rival.getNombre() + ": " + pregunta.getDescripcion()
                + " → " + (respuesta ? "Sí" : "No"));
        activo.registrarRespuesta(pregunta, respuesta);
        return false;
    }

    private void pausa() {
        System.out.print("(Enter para continuar) ");
        teclado.nextLine();
    }

    // ---------------------------------------------------------------
    // Marcador
    // ---------------------------------------------------------------

    public void mostrarMarcador() {
        Map<String, RegistroMarcador> tabla = marcador.leer();
        System.out.println("\n=== Marcador de récords ===");
        if (tabla.isEmpty()) {
            System.out.println("Todavía no hay partidas registradas.");
            return;
        }
        tabla.forEach((nombre, registro) -> {
            String mejor = registro.tieneVictorias() ? (registro.getMejorIntentos() + " intento(s)") : "sin victorias";
            System.out.println(nombre + " -> victorias: " + registro.getVictorias()
                    + " | derrotas: " + registro.getDerrotas()
                    + " | mejor victoria: " + mejor);
        });
    }

    // ---------------------------------------------------------------
    // Utilidades
    // ---------------------------------------------------------------

    private void mostrarPersonajes(List<Personaje> personajes) {
        System.out.println("Personajes disponibles:");
        for (Personaje p : personajes) {
            System.out.println(p);
        }
    }

    private Personaje buscarPorId(List<Personaje> personajes, int id) {
        for (Personaje p : personajes) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    private int leerEntero() {
        while (!teclado.hasNextInt()) {
            System.out.print("Ingresá un número válido: ");
            teclado.next();
        }
        int valor = teclado.nextInt();
        teclado.nextLine();
        return valor;
    }
}
