package model;

import java.util.Date;

public class MovimentacaoEstoque {
    private Integer id_movimentacao;
    private Integer id_medicamento;
    private TipoMovimentacao tipo;
    private Double quantidade;
    private Double estoque_antes;
    private Double estoque_depois;
    private Integer id_historico_uso;
    private Integer id_registrado_por;
    private String observacao;
    private Date data_movimentacao;

    public MovimentacaoEstoque(){}

    public MovimentacaoEstoque(Integer id_movimentacao, Integer id_medicamento, TipoMovimentacao tipo, Double quantidade, Double estoque_antes, Double estoque_depois, Integer id_historico_uso, Integer id_registrado_por, String observacao, Date data_movimentacao) {
        this.id_movimentacao = id_movimentacao;
        this.id_medicamento = id_medicamento;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.estoque_antes = estoque_antes;
        this.estoque_depois = estoque_depois;
        this.id_historico_uso = id_historico_uso;
        this.id_registrado_por = id_registrado_por;
        this.observacao = observacao;
        this.data_movimentacao = data_movimentacao;
    }

    public Integer getId_movimentacao() { return id_movimentacao; }
    public Integer getId_medicamento() { return id_medicamento; }
    public TipoMovimentacao getTipo() { return tipo; }
    public Double getQuantidade() { return quantidade; }
    public Double getEstoque_antes() { return estoque_antes; }
    public Double getEstoque_depois() { return estoque_depois; }
    public Integer getId_historico_uso() { return id_historico_uso; }
    public Integer getId_registrado_por() { return id_registrado_por; }
    public String getObservacao() { return observacao; }
    public Date getData_movimentacao() { return data_movimentacao; }

    public void setId_movimentacao(Integer id_movimentacao) { this.id_movimentacao = id_movimentacao; }
    public void setId_medicamento(Integer id_medicamento) { this.id_medicamento = id_medicamento; }
    public void setTipo(TipoMovimentacao tipo) { this.tipo = tipo; }
    public void setQuantidade(Double quantidade) { this.quantidade = quantidade; }
    public void setEstoque_antes(Double estoque_antes) { this.estoque_antes = estoque_antes; }
    public void setEstoque_depois(Double estoque_depois) { this.estoque_depois = estoque_depois; }
    public void setId_historico_uso(Integer id_historico_uso) { this.id_historico_uso = id_historico_uso; }
    public void setId_registrado_por(Integer id_registrado_por) { this.id_registrado_por = id_registrado_por; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public void setData_movimentacao(Date data_movimentacao) { this.data_movimentacao = data_movimentacao; }

    @Override
    public String toString() {
        return "MovimentacaoEstoque [id_movimentacao=" + id_movimentacao + ", id_medicamento=" + id_medicamento + ", tipo=" + tipo + ", quantidade=" + quantidade + ", estoque_antes=" + estoque_antes + ", estoque_depois=" + estoque_depois + ", id_historico_uso=" + id_historico_uso + ", id_registrado_por=" + id_registrado_por + ", observacao=" + observacao + ", data_movimentacao=" + data_movimentacao + "]";
    }
}
