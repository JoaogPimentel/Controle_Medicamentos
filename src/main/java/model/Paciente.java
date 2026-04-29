package model;
import java.sql.Date;

public class Paciente {
    private int id_pessoa;
    private String convenio;
    private String observacoes_medicas;
    private Date data_virou_paciente;
    private Date data_atualizacao;
    
    public Paciente(){}

    public Paciente(int id_pessoa, String convenio, String observacoes_medicas, Date data_virou_paciente, Date data_atualizacao) {
        this.id_pessoa = id_pessoa;
        this.convenio = convenio;
        this.observacoes_medicas = observacoes_medicas;
        this.data_virou_paciente = data_virou_paciente;
        this.data_atualizacao = data_atualizacao;
    }
    public int getId_pessoa() { return id_pessoa; }
    public String getConvenio() { return convenio; }
    public String getObservacoes_medicas() { return observacoes_medicas; }
    public Date getData_virou_paciente() { return data_virou_paciente; }
    public Date getData_atualizacao() { return data_atualizacao; }

    public void setId_pessoa(int id_pessoa) { this.id_pessoa = id_pessoa; }
    public void setConvenio(String convenio) { this.convenio = convenio; }
    public void setObservacoes_medicas(String observacoes_medicas) { this.observacoes_medicas = observacoes_medicas; }
    public void setData_virou_paciente(Date data_virou_paciente) { this.data_virou_paciente = data_virou_paciente; }
    public void setData_atualizacao(Date data_atualizacao) { this.data_atualizacao = data_atualizacao; }
}
