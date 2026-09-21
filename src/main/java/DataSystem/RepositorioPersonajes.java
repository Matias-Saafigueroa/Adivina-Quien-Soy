package DataSystem;

import Modelos.ColorPelo;
import Modelos.Edad;
import Modelos.Genero;
import Modelos.Grupo;
import Modelos.Personaje;
import Servicios.Ordenamiento;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * Carga el tablero de 23 personajes. El archivo fuente los trae sin un orden fijo; es
 * este repositorio quien los dispone en una lista ordenada por género (con Merge Sort,
 * que es estable, así que dentro de cada género se respeta el orden del archivo) y
 * les asigna un id autoincremental a medida que los va agregando a esa lista.
 */
public class RepositorioPersonajes {

    /** Criterio de ordenamiento: primero MASCULINO y después FEMENINO (orden de declaración del enum). */
    private static final Comparator<Personaje> POR_GENERO = Comparator.comparing(Personaje::getGenero);

    private final String archivo;

    public RepositorioPersonajes(String archivo) {
        this.archivo = archivo;
    }

    /** Devuelve los personajes ordenados por género y con ids 1, 2, 3... según su lugar en la lista. */
    public List<Personaje> cargar() {
        List<Personaje> ordenados = Ordenamiento.mergeSort(cargarSinOrdenar(), POR_GENERO);

        List<Personaje> personajes = new ArrayList<>();
        int siguienteId = 1;
        for (Personaje p : ordenados) {
            personajes.add(new Personaje(siguienteId, p.getNombre(), p.getGenero(),
                    p.isCalvo(), p.isLentes(), p.getColorPelo(), p.getEdad(), p.getGrupo()));
            siguienteId++;
        }
        return personajes;
    }

    /** Los personajes tal como están en el archivo (ids provisorios 0). Sirve para comparar algoritmos de ordenamiento. */
    public List<Personaje> cargarSinOrdenar() {
        List<Personaje> personajes = new ArrayList<>();
        try (Scanner sc = new Scanner(new File(archivo))) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                if (linea.isBlank()) continue;
                String[] partes = linea.split(",");
                String nombre = partes[0].trim();
                Genero genero = Genero.valueOf(partes[1].trim());
                boolean calvo = Boolean.parseBoolean(partes[2].trim());
                boolean lentes = Boolean.parseBoolean(partes[3].trim());
                ColorPelo colorPelo = ColorPelo.valueOf(partes[4].trim());
                Edad edad = Edad.valueOf(partes[5].trim());
                Grupo grupo = Grupo.valueOf(partes[6].trim());
                personajes.add(new Personaje(0, nombre, genero, calvo, lentes, colorPelo, edad, grupo));
            }
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("No se encontró el archivo de personajes: " + archivo, e);
        }
        return personajes;
    }
}
