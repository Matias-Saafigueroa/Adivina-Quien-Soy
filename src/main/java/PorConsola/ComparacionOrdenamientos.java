package PorConsola;

import DataSystem.RepositorioPersonajes;
import Modelos.Personaje;
import Servicios.Ordenamiento;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Compara Merge Sort (Divide y Conquista, O(n log n)) contra Inserción (cuadrático, O(n²)).
 * Primero sobre la lista real de 23 personajes y después sobre listas más grandes para ver
 * cómo crece cada uno. Se corre con Run 'ComparacionOrdenamientos.main()'.
 */
public class ComparacionOrdenamientos {

    private static final String ARCHIVO_PERSONAJES = "src/Data/personajes.txt";
    private static final Comparator<Personaje> POR_GENERO = Comparator.comparing(Personaje::getGenero);

    private static long comparaciones;

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        List<Personaje> personajes = new RepositorioPersonajes(ARCHIVO_PERSONAJES).cargarSinOrdenar();
        Comparator<Personaje> contador = (a, b) -> {
            comparaciones++;
            return POR_GENERO.compare(a, b);
        };

        calentar(personajes, contador);

        System.out.println("=== 23 personajes reales, criterio: género (promedio de 20000 corridas) ===");
        System.out.println("Algoritmo      | Tiempo por corrida (ms) | Comparaciones");
        fila("Merge Sort", personajes, contador, true, 20000);
        fila("Inserción", personajes, contador, false, 20000);

        System.out.println();
        System.out.println("=== Listas más grandes de enteros aleatorios (promedio de varias corridas) ===");
        System.out.println("n        | Merge Sort (ms) | Inserción (ms)");
        Random azar = new Random(42);
        for (int n : new int[]{23, 100, 1000, 10000, 50000}) {
            List<Integer> datos = new ArrayList<>();
            for (int i = 0; i < n; i++) datos.add(azar.nextInt(1_000_000));
            // Menos corridas cuanto más grande es la lista, para que el programa no tarde demasiado.
            int corridas = n <= 1000 ? 500 : (n <= 10000 ? 20 : 3);
            Runnable merge = () -> Ordenamiento.mergeSort(datos, Comparator.naturalOrder());
            Runnable insercion = () -> Ordenamiento.insercion(datos, Comparator.naturalOrder());
            tiempoMs(merge, corridas);      // calentamiento de la JVM
            tiempoMs(insercion, corridas);
            System.out.printf("%-8d | %15.4f | %14.4f%n", n, tiempoMs(merge, corridas), tiempoMs(insercion, corridas));
        }
    }

    private static void calentar(List<Personaje> personajes, Comparator<Personaje> criterio) {
        // La JVM optimiza el código a medida que se ejecuta: se descartan las primeras corridas.
        for (int i = 0; i < 20000; i++) {
            Ordenamiento.mergeSort(personajes, criterio);
            Ordenamiento.insercion(personajes, criterio);
        }
    }

    private static void fila(String nombre, List<Personaje> personajes, Comparator<Personaje> criterio,
                             boolean merge, int corridas) {
        comparaciones = 0;
        Ordenamiento.mergeSort(personajes, criterio);
        long comparacionesMerge = comparaciones;
        comparaciones = 0;
        Ordenamiento.insercion(personajes, criterio);
        long comparacionesInsercion = comparaciones;

        double ms = tiempoMs(() -> {
            if (merge) Ordenamiento.mergeSort(personajes, criterio);
            else Ordenamiento.insercion(personajes, criterio);
        }, corridas);
        System.out.printf("%-14s | %23.6f | %d%n", nombre, ms, merge ? comparacionesMerge : comparacionesInsercion);
    }

    /** Tiempo promedio en milisegundos de una corrida. */
    private static double tiempoMs(Runnable tarea, int corridas) {
        long inicio = System.nanoTime();
        for (int i = 0; i < corridas; i++) {
            tarea.run();
        }
        return (System.nanoTime() - inicio) / 1_000_000.0 / corridas;
    }
}
