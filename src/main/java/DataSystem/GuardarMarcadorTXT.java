package DataSystem;

import Modelos.RegistroMarcador;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Persiste el marcador de récord: por cada nombre de usuario, cuántas
 * partidas ganó, cuántas perdió, y en cuántos intentos logró su mejor
 * victoria.
 */
public class GuardarMarcadorTXT {

    private final String archivo;

    public GuardarMarcadorTXT(String archivo) {
        this.archivo = archivo;
    }

    public Map<String, RegistroMarcador> leer() {
        Map<String, RegistroMarcador> marcador = new LinkedHashMap<>();
        File f = new File(archivo);
        if (!f.exists()) return marcador;
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                if (linea.isBlank()) continue;
                String[] partes = linea.split(",");
                String nombre = partes[0].trim();
                int victorias = Integer.parseInt(partes[1].trim());
                // Formato viejo (nombre,victorias): se migra sin derrotas ni mejor intento conocidos.
                int derrotas = partes.length > 2 ? Integer.parseInt(partes[2].trim()) : 0;
                int mejorIntentos = partes.length > 3 ? Integer.parseInt(partes[3].trim()) : Integer.MAX_VALUE;
                marcador.put(nombre, new RegistroMarcador(victorias, derrotas, mejorIntentos));
            }
        } catch (IOException e) {
            System.out.println("No se pudo leer el marcador.");
        }
        return marcador;
    }

    public void sumarVictoria(String nombreUsuario, int intentos) {
        Map<String, RegistroMarcador> marcador = leer();
        RegistroMarcador registro = marcador.computeIfAbsent(nombreUsuario, n -> RegistroMarcador.vacio());
        registro.registrarVictoria(intentos);
        guardar(marcador);
    }

    public void sumarDerrota(String nombreUsuario) {
        Map<String, RegistroMarcador> marcador = leer();
        RegistroMarcador registro = marcador.computeIfAbsent(nombreUsuario, n -> RegistroMarcador.vacio());
        registro.registrarDerrota();
        guardar(marcador);
    }

    private void guardar(Map<String, RegistroMarcador> marcador) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(archivo, false))) {
            for (Map.Entry<String, RegistroMarcador> e : marcador.entrySet()) {
                RegistroMarcador r = e.getValue();
                pw.println(e.getKey() + "," + r.getVictorias() + "," + r.getDerrotas() + "," + r.getMejorIntentos());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
