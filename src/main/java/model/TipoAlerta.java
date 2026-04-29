package model;

public enum TipoAlerta {
    DOSE_PROXIMA("dose prevista"),
    DOSE_ATRASADA("dose atrasada"),
    ESTOQUE_BAIXO("estoque baixo"),
    ESTOQUE_ZERADO("estoque zerado"),
    VENCIMENTO_PROXIMO("vencimento próximo");

    private String alerta;

    TipoAlerta(String alerta) {
        this.alerta = alerta;
    }

    public String getAlerta() {
        return alerta;
    }
}
