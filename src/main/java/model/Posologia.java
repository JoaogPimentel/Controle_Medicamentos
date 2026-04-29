package model;

import java.sql.Time;
import java.util.Date;

public class Posologia {
    private Integer id_posologia;
    private Integer id_medicamento;
    private Time horario_primeira_dose;
    private Integer intervalo_horas;
    private Integer duracao_dias;
    private Double quantidade_por_dose;
    private Date data_inicio;
    private Boolean ativo;
    private Date data_cadastro;
    private Date data_atualizacao;

    public Posologia(){}

    public Posologia(Integer id_posologia, Integer id_medicamento, Time horario_primeira_dose, Integer intervalo_horas, Integer duracao_dias, Double quantidade_por_dose, Date data_inicio, Boolean ativo, Date data_cadastro, Date data_atualizacao) {
        this.id_posologia = id_posologia;
        this.id_medicamento = id_medicamento;
        this.horario_primeira_dose = horario_primeira_dose;
        this.intervalo_horas = intervalo_horas;
        this.duracao_dias = duracao_dias;
        this.quantidade_por_dose = quantidade_por_dose;
        this.data_inicio = data_inicio;
        this.ativo = ativo;
        this.data_cadastro = data_cadastro;
        this.data_atualizacao = data_atualizacao;
    }

    public Integer getId_posologia() { return id_posologia; }
    public Integer getId_medicamento() { return id_medicamento; }
    public Time getHorario_primeira_dose() { return horario_primeira_dose; }
    public Integer getIntervalo_horas() { return intervalo_horas; }
    public Integer getDuracao_dias() { return duracao_dias; }
    public Double getQuantidade_por_dose() { return quantidade_por_dose; }
    public Date getData_inicio() { return data_inicio; }
    public Boolean getAtivo() { return ativo; }
    public Date getData_cadastro() { return data_cadastro; }
    public Date getData_atualizacao() { return data_atualizacao; }

    public void setId_posologia(Integer id_posologia) { this.id_posologia = id_posologia; }
    public void setId_medicamento(Integer id_medicamento) { this.id_medicamento = id_medicamento; }
    public void setHorario_primeira_dose(Time horario_primeira_dose) { this.horario_primeira_dose = horario_primeira_dose; }
    public void setIntervalo_horas(Integer intervalo_horas) { this.intervalo_horas = intervalo_horas; }
    public void setDuracao_dias(Integer duracao_dias) { this.duracao_dias = duracao_dias; }
    public void setQuantidade_por_dose(Double quantidade_por_dose) { this.quantidade_por_dose = quantidade_por_dose; }
    public void setData_inicio(Date data_inicio) { this.data_inicio = data_inicio; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public void setData_cadastro(Date data_cadastro) { this.data_cadastro = data_cadastro; }
    public void setData_atualizacao(Date data_atualizacao) { this.data_atualizacao = data_atualizacao; }

    @Override
    public String toString() {
        return "Posologia [id_posologia=" + id_posologia + ", id_medicamento=" + id_medicamento + ", horario_primeira_dose=" + horario_primeira_dose + ", intervalo_horas=" + intervalo_horas + ", duracao_dias=" + duracao_dias + ", quantidade_por_dose=" + quantidade_por_dose + ", data_inicio=" + data_inicio + ", ativo=" + ativo + ", data_cadastro=" + data_cadastro + ", data_atualizacao=" + data_atualizacao + "]";
    }
}
