package org.example;

import Servicios.Juego;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/** Punto de entrada de la versión por consola. */
public class Main {

    private static final String ARCHIVO_PERSONAJES = "src/Data/personajes.txt";
    private static final String ARCHIVO_MARCADOR = "src/Data/marcador.txt";

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        Scanner teclado = new Scanner(System.in, StandardCharsets.UTF_8);
        Juego juego = new Juego(ARCHIVO_PERSONAJES, ARCHIVO_MARCADOR, teclado);

        System.out.println("¡Bienvenido a Adivina Quién Soy!");

        boolean salir = false;
        while (!salir) {
            System.out.println("\n=== Menú principal ===");
            System.out.println("1) Jugar el desafío (Máquina 1 y después Máquina 2)");
            System.out.println("2) Modo espectador: Máquina vs Máquina");
            System.out.println("3) Ver marcador de récords");
            System.out.println("4) Salir");
            System.out.print("Elegí una opción: ");

            String opcion = teclado.nextLine().trim();
            switch (opcion) {
                case "1" -> juego.jugarDesafio();
                case "2" -> juego.jugarMaquinaVsMaquina();
                case "3" -> juego.mostrarMarcador();
                case "4" -> salir = true;
                default -> System.out.println("Opción inválida.");
            }
        }

        System.out.println("Gracias por jugar.");
    }
}
