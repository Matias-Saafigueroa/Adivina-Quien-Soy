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
 * Persiste el marcador de récord: por cada nombre de jugador, cuántas veces
 * le ganó a Máquina 1, cuántas veces completó el desafío ganándoles a las
 * dos, cuántas partidas perdió, y en cuántos intentos logró su mejor
 * desafío completo.
 *
 * Formato de cada línea: nombre,victoriasMaquina1,victoriasCompletas,derrotas,mejorIntentos
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
                marcador.put(nombre, parsearRegistro(partes));
            }
        } catch (IOException e) {
            System.out.println("No se pudo leer el marcador.");
        }
        return marcador;
    }

    private RegistroMarcador parsearRegistro(String[] partes) {
        if (partes.length >= 5) {
            return new RegistroMarcador(
                    Integer.parseInt(partes[1].trim()),
                    Integer.parseInt(partes[2].trim()),
                    Integer.parseInt(partes[3].trim()),
                    Integer.parseInt(partes[4].trim()));
        }
        // Formatos viejos: no distinguían victorias contra Máquina 1 de victorias completas.
        int victoriasCompletas = Integer.parseInt(partes[1].trim());
        int derrotas = partes.length > 2 ? Integer.parseInt(partes[2].trim()) : 0;
        int mejorIntentos = partes.length > 3 ? Integer.parseInt(partes[3].trim()) : Integer.MAX_VALUE;
        return new RegistroMarcador(0, victoriasCompletas, derrotas, mejorIntentos);
    }

    public void sumarVictoriaMaquina1(String nombreUsuario) {
        Map<String, RegistroMarcador> marcador = leer();
        RegistroMarcador registro = marcador.computeIfAbsent(nombreUsuario, n -> RegistroMarcador.vacio());
        registro.registrarVictoriaMaquina1();
        guardar(marcador);
    }

    public void sumarVictoriaCompleta(String nombreUsuario, int intentos) {
        Map<String, RegistroMarcador> marcador = leer();
        RegistroMarcador registro = marcador.computeIfAbsent(nombreUsuario, n -> RegistroMarcador.vacio());
        registro.registrarVictoriaCompleta(intentos);
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
                pw.println(e.getKey() + "," + r.getVictoriasMaquina1() + "," + r.getVictoriasCompletas()
                        + "," + r.getDerrotas() + "," + r.getMejorIntentos());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
