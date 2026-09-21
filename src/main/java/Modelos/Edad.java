package Modelos;

public enum Edad implements Descriptible {
    BEBE("es un bebé"),
    NINO("es un niño o una niña"),
    ADULTO("es adulto"),
    ANCIANO("es anciano");

    private final String clausula;

    Edad(String clausula) {
        this.clausula = clausula;
    }

    @Override
    public String clausula() {
        return clausula;
    }
}
