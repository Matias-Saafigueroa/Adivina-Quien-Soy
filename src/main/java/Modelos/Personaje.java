package Modelos;

public class Personaje {
    private final int id;
    private final String nombre;
    private final Genero genero;
    private final boolean calvo;
    private final boolean lentes;
    private final ColorPelo colorPelo;

    public Personaje(int id, String nombre, Genero genero, boolean calvo, boolean lentes, ColorPelo colorPelo) {
        this.id = id;
        this.nombre = nombre;
        this.genero = genero;
        this.calvo = calvo;
        this.lentes = lentes;
        this.colorPelo = colorPelo;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Genero getGenero() {
        return genero;
    }

    public boolean isCalvo() {
        return calvo;
    }

    public boolean isLentes() {
        return lentes;
    }

    public ColorPelo getColorPelo() {
        return colorPelo;
    }

    @Override
    public String toString() {
        return String.format("%2d - %-14s | Género: %-9s | Calvo: %-5s | Lentes: %-5s | Pelo: %s",
                id, nombre, genero, calvo, lentes, colorPelo);
    }
}
