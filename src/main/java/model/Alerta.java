package model;

import java.util.Date;

public class Alerta {
    private Integer id_alerta;
    private Integer id_pessoa;
    private Integer id_medicamento;
    private TipoAlerta tipo;
    private String mensagem;
    private Boolean lido;
    private Date data_geracao;
    private Date data_leitura;

    public Alerta(){}

    public Alerta(Integer id_alerta, Integer id_pessoa, Integer id_medicamento, TipoAlerta tipo, String mensagem, Boolean lido, Date data_geracao, Date data_leitura) {
        this.id_alerta = id_alerta;
        this.id_pessoa = id_pessoa;
        this.id_medicamento = id_medicamento;
        this.tipo = tipo;
        this.mensagem = mensagem;
        this.lido = lido;
        this.data_geracao = data_geracao;
        this.data_leitura = data_leitura;
    }

    public Integer getId_alerta() { return id_alerta; }
    public Integer getId_pessoa() { return id_pessoa; }
    public Integer getId_medicamento() { return id_medicamento; }
    public TipoAlerta getTipo() { return tipo; }
    public String getMensagem() { return mensagem; }
    public Boolean getLido() { return lido; }
    public Date getData_geracao() { return data_geracao; }
    public Date getData_leitura() { return data_leitura; }

    public void setId_alerta(Integer id_alerta) { this.id_alerta = id_alerta; }
    public void setId_pessoa(Integer id_pessoa) { this.id_pessoa = id_pessoa; }
    public void setId_medicamento(Integer id_medicamento) { this.id_medicamento = id_medicamento; }
    public void setTipo(TipoAlerta tipo) { this.tipo = tipo; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public void setLido(Boolean lido) { this.lido = lido; }
    public void setData_geracao(Date data_geracao) { this.data_geracao = data_geracao; }
    public void setData_leitura(Date data_leitura) { this.data_leitura = data_leitura; }

    @Override
    public String toString() {
        return "Alerta [id_alerta=" + id_alerta + ", id_pessoa=" + id_pessoa + ", id_medicamento=" + id_medicamento + ", tipo=" + tipo + ", mensagem=" + mensagem + ", lido=" + lido + ", data_geracao=" + data_geracao + ", data_leitura=" + data_leitura + "]";
    }
}
