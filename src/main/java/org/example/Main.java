package org.example;

import Servicios.Juego;
import Servicios.MaquinaAvanzada;
import Servicios.MaquinaBasica;
import DataSystem.RepositorioPersonajes;
import Modelos.Personaje;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String ARCHIVO_PERSONAJES = "src/Data/personajes.txt";
    private static final String ARCHIVO_MARCADOR = "src/Data/marcador.txt";
    private static final String ARCHIVO_LOG_MAQUINA1 = "src/Data/preguntasMaquina1.txt";

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        Scanner teclado = new Scanner(System.in, StandardCharsets.UTF_8);
        Juego juego = new Juego(ARCHIVO_PERSONAJES, ARCHIVO_MARCADOR, ARCHIVO_LOG_MAQUINA1, teclado);

        System.out.println("¡Bienvenido a Adivina Quién Soy!");

        boolean salir = false;
        while (!salir) {
            System.out.println("\n=== Menú principal ===");
            System.out.println("1) Jugar contra Máquina 1 (intuitiva)");
            System.out.println("2) Jugar contra Máquina 2 (analítica, con ventaja)");
            System.out.println("3) Modo espectador: Máquina vs Máquina");
            System.out.println("4) Ver marcador de récords");
            System.out.println("5) Salir");
            System.out.print("Elegí una opción: ");

            String opcion = teclado.nextLine().trim();
            switch (opcion) {
                case "1" -> jugarContraMaquina(juego, teclado, 1);
                case "2" -> jugarContraMaquina(juego, teclado, 2);
                case "3" -> juego.jugarMaquinaVsMaquina();
                case "4" -> juego.mostrarMarcador();
                case "5" -> salir = true;
                default -> System.out.println("Opción inválida.");
            }
        }

        System.out.println("Gracias por jugar.");
    }

    private static void jugarContraMaquina(Juego juego, Scanner teclado, int numeroMaquina) {
        System.out.print("Ingresá tu nombre de usuario: ");
        String nombreUsuario = teclado.nextLine().trim();
        if (nombreUsuario.isEmpty()) {
            nombreUsuario = "Jugador";
        }

        List<Personaje> personajes = new RepositorioPersonajes(ARCHIVO_PERSONAJES).cargar();
        if (numeroMaquina == 1) {
            juego.jugarContraMaquina(nombreUsuario, new MaquinaBasica(personajes, ARCHIVO_LOG_MAQUINA1));
        } else {
            juego.jugarContraMaquina(nombreUsuario, new MaquinaAvanzada(personajes, ARCHIVO_LOG_MAQUINA1));
        }
    }
}
