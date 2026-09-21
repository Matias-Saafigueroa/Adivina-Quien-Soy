package Servicios;

import DataSystem.GuardarMarcadorTXT;
import DataSystem.RepositorioPersonajes;
import Modelos.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Versión por consola del juego. No tiene reglas propias: usa Partida y Espectador,
 * las mismas clases que usa la ventana, y solo se encarga de leer del teclado y
 * de imprimir en pantalla.
 */
public class Juego {

    private final RepositorioPersonajes repositorioPersonajes;
    private final String archivoMarcador;
    private final GuardarMarcadorTXT marcador;
    private final Scanner teclado;

    public Juego(String archivoPersonajes, String archivoMarcador, Scanner teclado) {
        this.repositorioPersonajes = new RepositorioPersonajes(archivoPersonajes);
        this.archivoMarcador = archivoMarcador;
        this.marcador = new GuardarMarcadorTXT(archivoMarcador);
        this.teclado = teclado;
    }

    // ---------------------------------------------------------------
    // Desafío: Humano vs Máquina 1 y después vs Máquina 2
    // ---------------------------------------------------------------

    public void jugarDesafio() {
        List<Personaje> personajes = repositorioPersonajes.cargar();

        System.out.print("Ingresá tu nombre de usuario: ");
        String nombreUsuario = teclado.nextLine().trim();
        if (nombreUsuario.isEmpty()) {
            nombreUsuario = "Jugador";
        }

        Personaje secreto = elegirPersonaje(personajes);
        Partida partida = new Partida(archivoMarcador, personajes, nombreUsuario, secreto);
        mostrar(partida.mensajesDeInicio());

        Set<String> preguntasHechas = new HashSet<>();
        Partida.Turno turno;
        do {
            turno = turnoDelJugador(partida, personajes, preguntasHechas);
            mostrar(turno.mensajes());
            if (turno.fin() == Partida.Fin.PASA_A_RONDA_2) {
                preguntasHechas.clear();
            }
        } while (turno.fin() == Partida.Fin.SIGUE || turno.fin() == Partida.Fin.PASA_A_RONDA_2);

        mostrarMarcador();
    }

    private Personaje elegirPersonaje(List<Personaje> personajes) {
        Personaje elegido = null;
        while (elegido == null) {
            mostrarPersonajes(personajes);
            System.out.print("Elegí tu personaje secreto por id: ");
            elegido = buscarPorId(personajes, leerEntero());
            if (elegido == null) {
                System.out.println("Ese id no existe, probá de nuevo.");
            }
        }
        System.out.println("Tu personaje secreto es " + elegido.getNombre() + ".");
        return elegido;
    }

    private Partida.Turno turnoDelJugador(Partida partida, List<Personaje> personajes, Set<String> preguntasHechas) {
        System.out.println("\n--- Ronda " + partida.getRonda() + " contra " + partida.getNombreMaquina()
                + " - Turno " + partida.getTurno() + " ---");
        List<Personaje> candidatos = partida.getCandidatosHumano();
        mostrarPersonajes(candidatos);
        System.out.println("(" + candidatos.size() + " personaje(s) posible(s))");

        while (true) {
            System.out.println("1) Preguntar   2) Adivinar (si fallás, perdés el desafío)   3) Rendirme");
            System.out.print("Elegí una opción: ");
            switch (leerEntero()) {
                case 1 -> {
                    Pregunta pregunta = elegirPregunta(preguntasHechas);
                    if (pregunta != null) {
                        return partida.preguntar(pregunta);
                    }
                }
                case 2 -> {
                    System.out.print("¿Quién creés que es? Ingresá el id: ");
                    Personaje adivinado = buscarPorId(personajes, leerEntero());
                    if (adivinado == null) {
                        System.out.println("Ese id no existe.");
                    } else {
                        return partida.adivinar(adivinado.getId());
                    }
                }
                case 3 -> {
                    return partida.rendirse();
                }
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    /** Muestra las preguntas que todavía no hiciste en esta ronda. @return null si volvés atrás o no quedan. */
    private Pregunta elegirPregunta(Set<String> preguntasHechas) {
        List<Pregunta> disponibles = new ArrayList<>();
        for (Pregunta p : todasLasPreguntas()) {
            if (!preguntasHechas.contains(p.getClave())) {
                disponibles.add(p);
            }
        }
        if (disponibles.isEmpty()) {
            System.out.println("Ya hiciste todas las preguntas en esta ronda: tenés que adivinar o rendirte.");
            return null;
        }

        for (int i = 0; i < disponibles.size(); i++) {
            System.out.println((i + 1) + ") ¿Su personaje " + disponibles.get(i).getClausula() + "?");
        }
        System.out.print("Elegí una pregunta (0 para volver): ");
        int posicion = leerEntero();
        if (posicion < 1 || posicion > disponibles.size()) {
            return null;
        }
        Pregunta elegida = disponibles.get(posicion - 1);
        preguntasHechas.add(elegida.getClave());
        return elegida;
    }

    private List<Pregunta> todasLasPreguntas() {
        List<Pregunta> preguntas = new ArrayList<>();
        preguntas.add(new Pregunta(Filtro.GENERO, Genero.MASCULINO));
        preguntas.add(new Pregunta(Filtro.GENERO, Genero.FEMENINO));
        preguntas.add(new Pregunta(Filtro.CALVICIE, Boolean.TRUE));
        preguntas.add(new Pregunta(Filtro.LENTES, Boolean.TRUE));
        for (ColorPelo color : ColorPelo.values()) {
            preguntas.add(new Pregunta(Filtro.COLOR_PELO, color));
        }
        for (Edad edad : Edad.values()) {
            preguntas.add(new Pregunta(Filtro.EDAD, edad));
        }
        for (Grupo grupo : Grupo.values()) {
            preguntas.add(new Pregunta(Filtro.GRUPO, grupo));
        }
        return preguntas;
    }

    // ---------------------------------------------------------------
    // Máquina vs Máquina (espectador)
    // ---------------------------------------------------------------

    public void jugarMaquinaVsMaquina() {
        Espectador espectador = new Espectador(repositorioPersonajes.cargar());
        mostrar(espectador.mensajesDeInicio());

        while (!espectador.isTerminada()) {
            System.out.print("(Enter para el siguiente paso) ");
            teclado.nextLine();
            mostrar(espectador.siguientePaso());
        }
        System.out.println("\nGanó " + espectador.getGanador() + ".");
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
            String mejor = registro.tieneVictoriasCompletas() ? registro.getMejorIntentos() + " intento(s)" : "-";
            System.out.println(nombre
                    + " -> le ganó a Máquina 1: " + registro.getVictoriasMaquina1()
                    + " | desafíos completos: " + registro.getVictoriasCompletas()
                    + " | derrotas: " + registro.getDerrotas()
                    + " | mejor desafío: " + mejor);
        });
    }

    // ---------------------------------------------------------------
    // Utilidades
    // ---------------------------------------------------------------

    private void mostrar(List<String> lineas) {
        for (String linea : lineas) {
            System.out.println(linea);
        }
    }

    private void mostrarPersonajes(List<Personaje> personajes) {
        System.out.println("Personajes:");
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
