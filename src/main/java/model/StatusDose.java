package model;

public enum StatusDose {
    PREVISTA("prevista"),
    TOMADA("tomada"),
    ATRASADA("atrasada"),
    PULADA("pulada");

    private String status;

    StatusDose(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}

