package DataSystem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Persiste el historial de preguntas (claves filtro=valor) que fue haciendo
 * la Máquina 1 a lo largo de las partidas. La Máquina 2 lo lee al arrancar
 * para tener ventaja sobre qué preguntas ya se probaron.
 */
public class GuardarPreguntasMaquinaTXT {

    private final String archivo;

    public GuardarPreguntasMaquinaTXT(String archivo) {
        this.archivo = archivo;
    }

    public void registrar(String claveDePregunta) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(archivo, true))) {
            pw.println(claveDePregunta);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<String> leerClaves() {
        Set<String> claves = new HashSet<>();
        File f = new File(archivo);
        if (!f.exists()) return claves;
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine().trim();
                if (!linea.isBlank()) claves.add(linea);
            }
        } catch (IOException e) {
            System.out.println("No se pudo leer el historial de preguntas.");
        }
        return claves;
    }
}
