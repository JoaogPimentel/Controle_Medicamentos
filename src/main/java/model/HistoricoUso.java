package model;

import java.util.Date;

public class HistoricoUso {
    private Integer id_historico;
    private Integer id_posologia;
    private Date data_hora;
    private StatusDose status;
    private Integer id_registrado_por;
    private String observacao;
    private Date data_registro;

    public HistoricoUso(){}

    public HistoricoUso(Integer id_historico, Integer id_posologia, Date data_hora, StatusDose status, Integer id_registrado_por, String observacao, Date data_registro) {
        this.id_historico = id_historico;
        this.id_posologia = id_posologia;
        this.data_hora = data_hora;
        this.status = status;
        this.id_registrado_por = id_registrado_por;
        this.observacao = observacao;
        this.data_registro = data_registro;
    }

    public Integer getId_historico() { return id_historico; }
    public Integer getId_posologia() { return id_posologia; }
    public Date getData_hora() { return data_hora; }
    public StatusDose getStatus() { return status; }
    public Integer getId_registrado_por() { return id_registrado_por; }
    public String getObservacao() { return observacao; }
    public Date getData_registro() { return data_registro; }

    public void setId_historico(Integer id_historico) { this.id_historico = id_historico; }
    public void setId_posologia(Integer id_posologia) { this.id_posologia = id_posologia; }
    public void setData_hora(Date data_hora) { this.data_hora = data_hora; }
    public void setStatus(StatusDose status) { this.status = status; }
    public void setId_registrado_por(Integer id_registrado_por) { this.id_registrado_por = id_registrado_por; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public void setData_registro(Date data_registro) { this.data_registro = data_registro; }

    @Override
    public String toString() {
        return "HistoricoUso [id_historico=" + id_historico + ", id_posologia=" + id_posologia + ", data_hora=" + data_hora + ", status=" + status + ", id_registrado_por=" + id_registrado_por + ", observacao=" + observacao + ", data_registro=" + data_registro + "]";
    }
}
