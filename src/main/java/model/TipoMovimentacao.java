package model;

public enum TipoMovimentacao {
    ENTRADA_COMPRA("entrada compra"),
    ENTRADA_AJUSTE("entrada ajuste"),
    SAIDA_DOSE("saida dose"),
    SAIDA_AJUSTE("saida ajuste"),
    SAIDA_DESCARTE("saida descarte");

    private String tipo;

    TipoMovimentacao(String tipo) {
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }
}
