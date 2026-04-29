package model;
import java.sql.Date;

public class Cuidador {
    private int id_pessoa;
    private boolean profissional_saude;
    private String registro_profissional;
    private Date data_virou_cuidador;
    private Date data_atualizacao;

    public Cuidador(){}

    public Cuidador(int id_pessoa, boolean profissional_saude, String registro_profissional, Date data_virou_cuidador, Date data_atualizacao) {
        this.id_pessoa = id_pessoa;
        this.profissional_saude = profissional_saude;
        this.registro_profissional = registro_profissional;
        this.data_virou_cuidador = data_virou_cuidador;
        this.data_atualizacao = data_atualizacao;
    }
    public int getId_pessoa() { return id_pessoa; }
    public boolean isProfissional_saude() { return profissional_saude; }
    public String getRegistro_profissional() { return registro_profissional; }
    public Date getData_virou_cuidador() { return data_virou_cuidador; }
    public Date getData_atualizacao() { return data_atualizacao; }

    public void setId_pessoa(int id_pessoa) { this.id_pessoa = id_pessoa; }
    public void setProfissional_saude(boolean profissional_saude) { this.profissional_saude = profissional_saude; }
    public void setRegistro_profissional(String registro_profissional) { this.registro_profissional = registro_profissional; }
    public void setData_virou_cuidador(Date data_virou_cuidador) { this.data_virou_cuidador = data_virou_cuidador; }
    public void setData_atualizacao(Date data_atualizacao) { this.data_atualizacao = data_atualizacao; }
}
