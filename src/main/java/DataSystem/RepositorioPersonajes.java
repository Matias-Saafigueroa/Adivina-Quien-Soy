package DataSystem;

import Modelos.ColorPelo;
import Modelos.Genero;
import Modelos.Personaje;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Carga el tablero de 23 personajes. El archivo fuente sólo los agrupa por
 * género; es este repositorio quien los dispone en una lista ordenada,
 * asignándoles un id autoincremental a medida que los va agregando.
 */
public class RepositorioPersonajes {

    private final String archivo;

    public RepositorioPersonajes(String archivo) {
        this.archivo = archivo;
    }

    public List<Personaje> cargar() {
        List<Personaje> personajes = new ArrayList<>();
        int siguienteId = 1;
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
                personajes.add(new Personaje(siguienteId, nombre, genero, calvo, lentes, colorPelo));
                siguienteId++;
            }
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("No se encontró el archivo de personajes: " + archivo, e);
        }
        return personajes;
    }
}
