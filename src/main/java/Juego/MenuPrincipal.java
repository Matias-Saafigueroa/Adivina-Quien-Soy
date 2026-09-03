package Juego;

import Servicios.Juego;

import java.util.Scanner;

/**
 * Interfaz de consola: muestra el menú principal
 */
public class MenuPrincipal {

    private final Juego juego;
    private final Scanner teclado;

    public MenuPrincipal(Juego juego, Scanner teclado) {
        this.juego = juego;
        this.teclado = teclado;
    }

    public void iniciar() {
        System.out.println("¡Bienvenido a Adivina Quién Soy!");

        boolean salir = false;
        while (!salir) {
            System.out.println("\n=== Menú principal ===");
            System.out.println("1) Jugar contra las máquinas (primero Máquina 1, y si le ganás, Máquina 2)");
            System.out.println("2) Modo espectador: Máquina vs Máquina");
            System.out.println("3) Ver marcador de récords");
            System.out.println("4) Salir");
            System.out.print("Elegí una opción: ");

            String opcion = teclado.nextLine().trim();
            switch (opcion) {
                case "1" -> jugarContraLasMaquinas();
                case "2" -> juego.jugarMaquinaVsMaquina();
                case "3" -> juego.mostrarMarcador();
                case "4" -> salir = true;
                default -> System.out.println("Opción inválida.");
            }
        }

        System.out.println("Gracias por jugar.");
    }

    private void jugarContraLasMaquinas() {
        System.out.print("Ingresá tu nombre de usuario: ");
        String nombreUsuario = teclado.nextLine().trim();
        if (nombreUsuario.isEmpty()) {
            nombreUsuario = "Jugador";
        }
        juego.jugarContraLasMaquinas(nombreUsuario);
    }
}
