import DataSystem.RepositorioPersonajes;
import Modelos.Personaje;
import Vista.ControlEspectador;
import Vista.ControlInicio;
import Vista.ControlJuego;
import Vista.ControlMarcador;
import Vista.ControlMenu;
import Vista.Tema;

import javax.swing.*;
import java.awt.CardLayout;
import java.util.List;

/**
 * Ventana principal. El diseño de todas las pantallas está en Jframe.form; acá solo
 * se cambia de pantalla (CardLayout) y se conecta cada pantalla con su controlador.
 */
public class Jframe {
    private JPanel PanelPrincipal;

    // panelMenu
    private JPanel panelMenu;
    private JButton botonSalir;
    private JButton botonMarcador;
    private JButton botonEspectear;
    private JButton botonJugar;

    // panelInicio
    private JPanel panelInicio;
    private JTextField txtNombre;
    private JButton empezarPartidaButton;
    private JButton cancelarButton;
    private JPanel panelPersonajes;
    private JLabel lblElegido;

    // panelJuego
    private JPanel panelJuego;
    private JLabel lblRonda;
    private JLabel lblCandidatos;
    private JPanel panelTablero;
    private JLabel lblTuPersonaje;
    private JComboBox<String> comboPreguntas;
    private JButton btnPreguntar;
    private JButton btnRendirse;
    private JTextArea txtRegistro;

    // panelEspectador
    private JPanel panelEspectador;
    private JLabel lblNombreMaquina1;
    private JLabel lblSecretoMaquina1;
    private JLabel lblCandidatosMaquina1;
    private JPanel panelTablero1;
    private JLabel lblNombreMaquina2;
    private JLabel lblSecretoMaquina2;
    private JLabel lblCandidatosMaquina2;
    private JPanel panelTablero2;
    private JTextArea txtEspectador;
    private JButton btnSiguientePaso;
    private JButton btnNuevaPartida;
    private JButton btnVolverEspectador;

    // panelMarcador
    private JPanel panelMarcador;
    private JTable tablaMarcador;
    private JButton btnVolverMarcador;

    private static final String CARD_MENU = "menu";
    private static final String CARD_INICIO = "inicio";
    private static final String CARD_JUEGO = "juego";
    private static final String CARD_ESPECTADOR = "espectador";
    private static final String CARD_MARCADOR = "marcador";

    private static final String ARCHIVO_PERSONAJES = "src/Data/personajes.txt";
    private static final String ARCHIVO_MARCADOR = "src/Data/marcador.txt";

    private final List<Personaje> personajes = cargarPersonajes();

    private ControlMenu controlMenu;
    private ControlInicio controlInicio;
    private ControlJuego controlJuego;
    private ControlEspectador controlEspectador;
    private ControlMarcador controlMarcador;

    public Jframe() {
        conectarControladores();
        ordenarPantallas();
        Tema.aplicar(PanelPrincipal);
        mostrar(CARD_MENU);
    }

    public JPanel getPanelPrincipal() {
        return PanelPrincipal;
    }

    // ---------------------------------------------------------------
    // Controladores: uno por pantalla
    // ---------------------------------------------------------------

    private void conectarControladores() {
        controlMenu = new ControlMenu(panelMenu, botonJugar, botonEspectear, botonMarcador, botonSalir,
                this::irAlInicio, this::irAlEspectador, this::irAlMarcador);

        controlInicio = new ControlInicio(txtNombre, lblElegido, panelPersonajes, empezarPartidaButton,
                cancelarButton, personajes, () -> mostrar(CARD_MENU), this::empezarPartida);

        controlJuego = new ControlJuego(ARCHIVO_MARCADOR, this::irAlMarcador, lblRonda, lblCandidatos,
                lblTuPersonaje, panelTablero, comboPreguntas, btnPreguntar, btnRendirse, txtRegistro);

        controlEspectador = new ControlEspectador(
                new ControlEspectador.Lado(lblNombreMaquina1, lblSecretoMaquina1, lblCandidatosMaquina1, panelTablero1),
                new ControlEspectador.Lado(lblNombreMaquina2, lblSecretoMaquina2, lblCandidatosMaquina2, panelTablero2),
                txtEspectador, btnSiguientePaso, btnNuevaPartida, btnVolverEspectador, () -> mostrar(CARD_MENU));

        controlMarcador = new ControlMarcador(ARCHIVO_MARCADOR, tablaMarcador, btnVolverMarcador,
                () -> mostrar(CARD_MENU));
    }

    // ---------------------------------------------------------------
    // Cambio de pantallas
    // ---------------------------------------------------------------

    /** Le pone nombre a cada pantalla; la primera que se agrega (el menú) es la que se ve al abrir. */
    private void ordenarPantallas() {
        PanelPrincipal.removeAll();
        PanelPrincipal.add(controlMenu.getPantalla(), CARD_MENU);
        PanelPrincipal.add(panelInicio, CARD_INICIO);
        PanelPrincipal.add(panelJuego, CARD_JUEGO);
        PanelPrincipal.add(panelEspectador, CARD_ESPECTADOR);
        PanelPrincipal.add(panelMarcador, CARD_MARCADOR);
    }

    private void mostrar(String pantalla) {
        ((CardLayout) PanelPrincipal.getLayout()).show(PanelPrincipal, pantalla);
    }

    private void irAlInicio() {
        controlInicio.reiniciar();
        mostrar(CARD_INICIO);
    }

    private void empezarPartida(String nombreUsuario, Personaje personajeElegido) {
        controlJuego.empezar(nombreUsuario, personajeElegido, personajes);
        mostrar(CARD_JUEGO);
    }

    private void irAlEspectador() {
        if (personajes.isEmpty()) return;
        controlEspectador.nuevaPartida(personajes);
        mostrar(CARD_ESPECTADOR);
    }

    private void irAlMarcador() {
        controlMarcador.actualizar();
        mostrar(CARD_MARCADOR);
    }

    // ---------------------------------------------------------------
    // Datos
    // ---------------------------------------------------------------

    /** Lee los 23 personajes; si no se encuentra el archivo, avisa y sigue con la lista vacía. */
    private List<Personaje> cargarPersonajes() {
        try {
            return new RepositorioPersonajes(ARCHIVO_PERSONAJES).cargar();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo cargar " + ARCHIVO_PERSONAJES + "\nCarpeta de trabajo: " + System.getProperty("user.dir"),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return List.of();
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
