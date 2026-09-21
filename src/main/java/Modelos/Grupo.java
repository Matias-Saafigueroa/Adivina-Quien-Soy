package Modelos;

public enum Grupo implements Descriptible {
    FAMILIA_SIMPSON("es de la familia Simpson"),
    FAMILIA_BOUVIER("es de la familia Bouvier"),
    ESCUELA("va o trabaja en la escuela de Springfield"),
    NEGOCIOS("tiene un negocio o un programa de televisión"),
    VECINOS("es vecino de los Simpson o de la iglesia");

    private final String clausula;

    Grupo(String clausula) {
        this.clausula = clausula;
    }

    @Override
    public String clausula() {
        return clausula;
    }
}
