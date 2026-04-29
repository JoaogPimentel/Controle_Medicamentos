package model;
import java.sql.Date;

public class Pessoa {
    private int id_pessoa;
    private String nome;
    private Date data_nascimento;
    private String email;
    private String senha_hash;
    private String telefone;
    private Boolean ativo;
    private Date data_cadastro;
    private Date data_atualizacao;

    public Pessoa(){}

    public Pessoa(int id_pessoa, String nome, Date data_nascimento, String email, String senha_hash, String telefone) {
        this.id_pessoa = id_pessoa;
        this.nome = nome;
        this.data_nascimento = data_nascimento;
        this.email = email;
        this.senha_hash = senha_hash;
        this.telefone = telefone;
        this.ativo = true;
        this.data_cadastro = new Date(System.currentTimeMillis());
        this.data_atualizacao = new Date(System.currentTimeMillis());
    }

    public int getId_pessoa() { return id_pessoa; }
    public String getNome() { return nome; }
    public Date getData_nascimento() { return data_nascimento; }
    public String getEmail() { return email; }
    public String getSenha_hash() { return senha_hash; }
    public String getTelefone() { return telefone; }
    public Boolean getAtivo() { return ativo; }
    public Date getData_cadastro() { return data_cadastro; }
    public Date getData_atualizacao() { return data_atualizacao; }

    public void setId_pessoa(int id_pessoa) { this.id_pessoa = id_pessoa; }
    public void setNome(String nome) { this.nome = nome; }
    public void setData_nascimento(Date data_nascimento) { this.data_nascimento = data_nascimento; }
    public void setEmail(String email) { this.email = email; }
    public void setSenha_hash(String senha_hash) { this.senha_hash = senha_hash; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public void setData_cadastro(Date data_cadastro) { this.data_cadastro = data_cadastro; }
    public void setData_atualizacao(Date data_atualizacao) { this.data_atualizacao = data_atualizacao; }
}
