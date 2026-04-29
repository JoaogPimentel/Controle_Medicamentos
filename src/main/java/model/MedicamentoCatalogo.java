package model;

import java.sql.Date;

public class MedicamentoCatalogo {
    private Integer id_catalogo;
    private String nome;
    private String principio_ativo;
    private Date data_cadastro;
    private Date data_atualizacao;
    private FormaFarmaceutica forma_farmaceutica;

    public MedicamentoCatalogo(Integer id_catalogo, String nome, String principio_ativo, Date data_cadastro, Date data_atualizacao, FormaFarmaceutica forma_farmaceutica) {
        this.id_catalogo = id_catalogo;
        this.nome = nome;
        this.principio_ativo = principio_ativo;
        this.data_cadastro = data_cadastro;
        this.data_atualizacao = data_atualizacao;
        this.forma_farmaceutica = forma_farmaceutica;
    }

    public Integer getId_catalogo() { return id_catalogo; }
    public String getNome() { return nome; }
    public String getPrincipio_ativo() { return principio_ativo; }
    public Date getData_cadastro() { return data_cadastro; }
    public Date getData_atualizacao() { return data_atualizacao; }
    public FormaFarmaceutica getForma_farmaceutica() { return forma_farmaceutica; }

    public void setId_catalogo(Integer id_catalogo) { this.id_catalogo = id_catalogo; }
    public void setNome(String nome) { this.nome = nome; }
    public void setPrincipio_ativo(String principio_ativo) { this.principio_ativo = principio_ativo; }
    public void setData_cadastro(Date data_cadastro) { this.data_cadastro = data_cadastro; }
    public void setData_atualizacao(Date data_atualizacao) { this.data_atualizacao = data_atualizacao; }
    public void setForma_farmaceutica(FormaFarmaceutica forma_farmaceutica) { this.forma_farmaceutica = forma_farmaceutica; }

    @Override
    public String toString() {
        return "Medicamento [id_catalogo=" + id_catalogo + ", nome=" + nome + ", principio_ativo=" + principio_ativo
                + ", data_cadastro=" + data_cadastro + ", data_atualizacao=" + data_atualizacao
                + ", forma_farmaceutica=" + forma_farmaceutica + "]";
    }

}
