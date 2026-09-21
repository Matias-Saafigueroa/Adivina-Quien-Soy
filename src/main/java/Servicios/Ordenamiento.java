package Servicios;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Algoritmos de ordenamiento del proyecto.
 *
 * mergeSort es el algoritmo de Divide y Conquista que usa la máquina para disponer
 * los 23 personajes al cargarlos. insercion (cuadrático) existe solo para poder
 * comparar tiempos contra mergeSort en ComparacionOrdenamientos.
 */
public final class Ordenamiento {

    private Ordenamiento() {
    }

    /**
     * Merge Sort. Divide la lista en dos mitades, ordena cada mitad recursivamente y
     * combina las dos mitades ya ordenadas. Es estable y siempre O(n log n).
     * No modifica la lista recibida: devuelve una lista nueva.
     */
    public static <T> List<T> mergeSort(List<T> lista, Comparator<? super T> criterio) {
        // Caso base: una lista de 0 o 1 elementos ya está ordenada.
        if (lista.size() <= 1) {
            return new ArrayList<>(lista);
        }
        // Dividir
        int medio = lista.size() / 2;
        List<T> izquierda = mergeSort(lista.subList(0, medio), criterio);
        List<T> derecha = mergeSort(lista.subList(medio, lista.size()), criterio);
        // Combinar
        return combinar(izquierda, derecha, criterio);
    }

    /** Mezcla dos listas ya ordenadas en una sola, recorriéndolas una vez: O(n). */
    private static <T> List<T> combinar(List<T> izquierda, List<T> derecha, Comparator<? super T> criterio) {
        List<T> resultado = new ArrayList<>(izquierda.size() + derecha.size());
        int i = 0;
        int j = 0;
        while (i < izquierda.size() && j < derecha.size()) {
            // Con "<= 0" y ante un empate sale primero el de la izquierda: eso hace al algoritmo estable.
            if (criterio.compare(izquierda.get(i), derecha.get(j)) <= 0) {
                resultado.add(izquierda.get(i++));
            } else {
                resultado.add(derecha.get(j++));
            }
        }
        while (i < izquierda.size()) {
            resultado.add(izquierda.get(i++));
        }
        while (j < derecha.size()) {
            resultado.add(derecha.get(j++));
        }
        return resultado;
    }

    /** Ordenamiento por inserción: O(n²) en el peor caso. Solo para comparar contra mergeSort. */
    public static <T> List<T> insercion(List<T> lista, Comparator<? super T> criterio) {
        List<T> resultado = new ArrayList<>(lista);
        for (int i = 1; i < resultado.size(); i++) {
            T actual = resultado.get(i);
            int j = i - 1;
            while (j >= 0 && criterio.compare(resultado.get(j), actual) > 0) {
                resultado.set(j + 1, resultado.get(j));
                j--;
            }
            resultado.set(j + 1, actual);
        }
        return resultado;
    }
}
