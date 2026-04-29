package model;

public enum StatusMedicamento {
    EM_USO("em uso"),
    EM_ESTOQUE("em estoque"),
    DESCARTADO("descartado"),
    ARQUIVADO("arquivado");

    private String status;

    StatusMedicamento(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
