package Juego;

import Servicios.Juego;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {

    private static final String ARCHIVO_PERSONAJES = "src/Data/personajes.txt";
    private static final String ARCHIVO_MARCADOR = "src/Data/marcador.txt";

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        Scanner teclado = new Scanner(System.in, StandardCharsets.UTF_8);

        Juego juego = new Juego(ARCHIVO_PERSONAJES, ARCHIVO_MARCADOR, teclado);
        new MenuPrincipal(juego, teclado).iniciar();
    }
}
