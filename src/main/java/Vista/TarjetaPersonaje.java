package Vista;

import Modelos.Personaje;

import javax.swing.BorderFactory;
import javax.swing.DefaultButtonModel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ItemEvent;

/**
 * Tarjeta seleccionable de un personaje: foto arriba y nombre abajo.
 * Todas miden lo mismo, así la grilla queda pareja.
 */
public class TarjetaPersonaje extends JToggleButton {

    public static final int FOTO_ANCHO = 80;
    public static final int FOTO_ALTO = 100;

    private static final Color COLOR_SELECCION = Tema.NARANJA;
    private static final Color FONDO_SELECCION = Tema.CREMA;

    private final Personaje personaje;

    public TarjetaPersonaje(Personaje personaje) {
        this(personaje, FOTO_ANCHO, FOTO_ALTO);
    }

    /** Tarjeta con la foto en un tamaño a elección (la tarjeta se agranda lo justo para el marco y el nombre). */
    public TarjetaPersonaje(Personaje personaje, int fotoAncho, int fotoAlto) {
        super(nombreLegible(personaje.getNombre()), Imagenes.foto(personaje.getNombre(), fotoAncho, fotoAlto));
        this.personaje = personaje;

        setVerticalTextPosition(SwingConstants.BOTTOM);
        setHorizontalTextPosition(SwingConstants.CENTER);
        setFont(getFont().deriveFont(Font.PLAIN, fotoAncho < 50 ? 9f : fotoAncho < 70 ? 10f : 11f));
        setFocusPainted(false);
        setMargin(new Insets(4, 2, 4, 2));
        setPreferredSize(new Dimension(fotoAncho + 20, fotoAlto + 36));
        setToolTipText(descripcion(personaje));
        aplicarEstilo(false);

        addItemListener(e -> aplicarEstilo(e.getStateChange() == ItemEvent.SELECTED));
    }

    public Personaje getPersonaje() {
        return personaje;
    }

    /** Para tableros que solo se miran (como el del espectador): la tarjeta no responde a los clics. */
    public void soloMostrar() {
        setModel(new DefaultButtonModel() {
            @Override
            public void setPressed(boolean b) {
            }

            @Override
            public void setArmed(boolean b) {
            }

            @Override
            public void setRollover(boolean b) {
            }
        });
        setFocusable(false);
    }

    /** Resalta la tarjeta con un borde verde (por ejemplo, el personaje que se estaba buscando). */
    public void marcarComoSecreto() {
        setBorder(BorderFactory.createLineBorder(new Color(0x2E8B57), 3));
        setBackground(new Color(0xDFF3E6));
    }

    private void aplicarEstilo(boolean seleccionada) {
        if (seleccionada) {
            setBorder(BorderFactory.createLineBorder(COLOR_SELECCION, 3));
            setBackground(FONDO_SELECCION);
        } else {
            setBorder(BorderFactory.createLineBorder(Tema.TINTA, 1));
            setBackground(Color.WHITE);
        }
    }

    /** "ComicBookGuy" -> "Comic Book Guy". */
    private static String nombreLegible(String nombre) {
        return nombre.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
    }

    public static String descripcion(Personaje p) {
        return String.format("%s: %s, %s, %s, pelo %s",
                nombreLegible(p.getNombre()),
                p.getGenero().name().toLowerCase(),
                p.isCalvo() ? "calvo" : "no calvo",
                p.isLentes() ? "usa lentes" : "sin lentes",
                p.getColorPelo().name().toLowerCase());
    }
}
