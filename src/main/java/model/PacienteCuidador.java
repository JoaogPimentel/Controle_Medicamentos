package model;
import java.sql.Date;

public class PacienteCuidador {
    private int id_vinculo;
    private int id_paciente;
    private int id_cuidador;
    private Date data_vinculo;
    private Date data_fim;
    private boolean ativo;
    private Date data_atualizacao;

    public PacienteCuidador(){}

    public PacienteCuidador(int id_vinculo, int id_paciente, int id_cuidador, Date data_vinculo, Date data_fim, boolean ativo, Date data_atualizacao) {
        this.id_vinculo = id_vinculo;
        this.id_paciente = id_paciente;
        this.id_cuidador = id_cuidador;
        this.data_vinculo = data_vinculo;
        this.data_fim = data_fim;
        this.ativo = ativo;
        this.data_atualizacao = data_atualizacao;
    }

    public int getId_vinculo() { return id_vinculo; }
    public int getId_paciente() { return id_paciente; }
    public int getId_cuidador() { return id_cuidador; }
    public Date getData_vinculo() { return data_vinculo; }
    public Date getData_fim() { return data_fim; }
    public boolean isAtivo() { return ativo; }
    public Date getData_atualizacao() { return data_atualizacao; }

    public void setId_vinculo(int id_vinculo) { this.id_vinculo = id_vinculo; }
    public void setId_paciente(int id_paciente) { this.id_paciente = id_paciente; }
    public void setId_cuidador(int id_cuidador) { this.id_cuidador = id_cuidador; }
    public void setData_vinculo(Date data_vinculo) { this.data_vinculo = data_vinculo; }
    public void setData_fim(Date data_fim) { this.data_fim = data_fim; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public void setData_atualizacao(Date data_atualizacao) { this.data_atualizacao = data_atualizacao; }
}
