package model;

public enum FormaFarmaceutica {
    COMPRIMIDO("comprimido"),
    CAPSULA("cápsula"),
    LIQUIDO_ML("líquido ml"),
    GOTAS("gotas"),
    INJECAO("injeção"),
    POMADA("pomada"),
    SPRAY("spray"),
    ADESIVO("adesivo"),
    OUTRO("outro");

    private String forma;

    FormaFarmaceutica(String forma) {
        this.forma = forma;
    }

    public String getForma() {
        return forma;
    }
}
