package Vista;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Carga las fotos de los personajes desde /imagenes (src/main/resources) y las
 * deja todas del mismo tamaño: cada foto se achica para entrar en una caja fija,
 * conservando su proporción y centrada sobre fondo transparente. Los archivos
 * originales no se modifican.
 */
public final class Imagenes {

    private static final String CARPETA = "/imagenes/";
    private static final String[] EXTENSIONES = {".png", ".jpg", ".jpeg"};
    private static final Map<String, Icon> CACHE = new HashMap<>();

    private Imagenes() {
    }

    /** Foto del personaje en una caja de ancho x alto; si no existe el archivo, un cuadro con "?". */
    public static Icon foto(String nombrePersonaje, int ancho, int alto) {
        String clave = nombrePersonaje + "@" + ancho + "x" + alto;
        return CACHE.computeIfAbsent(clave, k -> {
            BufferedImage original = leer(nombrePersonaje);
            return new ImageIcon(original == null ? marcadorDeFaltante(ancho, alto) : encajar(original, ancho, alto));
        });
    }

    private static BufferedImage leer(String nombrePersonaje) {
        String[] nombres = {nombrePersonaje, nombrePersonaje.toLowerCase()};
        for (String nombre : nombres) {
            for (String extension : EXTENSIONES) {
                URL url = Imagenes.class.getResource(CARPETA + nombre + extension);
                if (url != null) {
                    try {
                        BufferedImage imagen = ImageIO.read(url);
                        if (imagen != null) return imagen;
                    } catch (IOException e) {
                        // se prueba con el siguiente candidato
                    }
                }
            }
        }
        return null;
    }

    private static BufferedImage encajar(BufferedImage original, int ancho, int alto) {
        double escala = Math.min((double) ancho / original.getWidth(), (double) alto / original.getHeight());
        int anchoFinal = Math.max(1, (int) Math.round(original.getWidth() * escala));
        int altoFinal = Math.max(1, (int) Math.round(original.getHeight() * escala));

        BufferedImage achicada = achicar(original, anchoFinal, altoFinal);

        BufferedImage caja = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = caja.createGraphics();
        g.drawImage(achicada, (ancho - anchoFinal) / 2, (alto - altoFinal) / 2, null);
        g.dispose();
        return caja;
    }

    /** Achica a la mitad de a pasos mientras haga falta, para que las fotos grandes no queden con dientes de sierra. */
    private static BufferedImage achicar(BufferedImage origen, int anchoFinal, int altoFinal) {
        BufferedImage actual = aArgb(origen);
        while (actual.getWidth() / 2 >= anchoFinal && actual.getHeight() / 2 >= altoFinal) {
            actual = redimensionar(actual, actual.getWidth() / 2, actual.getHeight() / 2);
        }
        return redimensionar(actual, anchoFinal, altoFinal);
    }

    private static BufferedImage aArgb(BufferedImage origen) {
        if (origen.getType() == BufferedImage.TYPE_INT_ARGB) return origen;
        BufferedImage copia = new BufferedImage(origen.getWidth(), origen.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copia.createGraphics();
        g.drawImage(origen, 0, 0, null);
        g.dispose();
        return copia;
    }

    private static BufferedImage redimensionar(BufferedImage origen, int ancho, int alto) {
        BufferedImage destino = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = destino.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(origen, 0, 0, ancho, alto, null);
        g.dispose();
        return destino;
    }

    private static BufferedImage marcadorDeFaltante(int ancho, int alto) {
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0xDDDDDD));
        g.fillRoundRect(0, 0, ancho, alto, 12, 12);
        g.setColor(new Color(0x888888));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(12, alto / 2)));
        FontMetrics fm = g.getFontMetrics();
        g.drawString("?", (ancho - fm.stringWidth("?")) / 2, (alto - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
        return imagen;
    }
}
