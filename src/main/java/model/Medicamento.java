package model;
import java.sql.Date;

public class Medicamento {
    private Integer id_medicamento;
    private Integer id_catalogo;
    private Integer id_paciente;
    private Integer id_cadastrado_por;
    private String dosagem;
    private Double estoque_atual;
    private Double estoque_minimo;
    private Date data_validade;
    private StatusMedicamento status;
    private Date data_primeiro_uso;
    private Date data_cadastro;
    private Date data_atualizacao;

    public Medicamento(){}

    public Medicamento(Integer id_medicamento, Integer id_catalogo, Integer id_paciente, Integer id_cadastrado_por, String dosagem, Double estoque_atual, Double estoque_minimo, Date data_validade, StatusMedicamento status, Date data_primeiro_uso, Date data_cadastro, Date data_atualizacao) {
        this.id_medicamento = id_medicamento;
        this.id_catalogo = id_catalogo;
        this.id_paciente = id_paciente;
        this.id_cadastrado_por = id_cadastrado_por;
        this.dosagem = dosagem;
        this.estoque_atual = estoque_atual;
        this.estoque_minimo = estoque_minimo;
        this.data_validade = data_validade;
        this.status = status;
        this.data_primeiro_uso = data_primeiro_uso;
        this.data_cadastro = data_cadastro;
        this.data_atualizacao = data_atualizacao;
    }

    public Integer getId_medicamento() { return id_medicamento; }
    public Integer getId_catalogo() { return id_catalogo; }
    public Integer getId_paciente() { return id_paciente; }
    public Integer getId_cadastrado_por() { return id_cadastrado_por; }
    public String getDosagem() { return dosagem; }
    public Double getEstoque_atual() { return estoque_atual; }
    public Double getEstoque_minimo() { return estoque_minimo; }
    public Date getData_validade() { return data_validade; }
    public StatusMedicamento getStatus() { return status; }
    public Date getData_primeiro_uso() { return data_primeiro_uso; }
    public Date getData_cadastro() { return data_cadastro; }
    public Date getData_atualizacao() { return data_atualizacao; }

    public void setId_medicamento(Integer id_medicamento) { this.id_medicamento = id_medicamento; }
    public void setId_catalogo(Integer id_catalogo) { this.id_catalogo = id_catalogo; }
    public void setId_paciente(Integer id_paciente) { this.id_paciente = id_paciente; }
    public void setId_cadastrado_por(Integer id_cadastrado_por) { this.id_cadastrado_por = id_cadastrado_por; }
    public void setDosagem(String dosagem) { this.dosagem = dosagem; }
    public void setEstoque_atual(Double estoque_atual) { this.estoque_atual = estoque_atual; }
    public void setEstoque_minimo(Double estoque_minimo) { this.estoque_minimo = estoque_minimo; }
    public void setData_validade(Date data_validade) { this.data_validade = data_validade; }
    public void setStatus(StatusMedicamento status) { this.status = status; }
    public void setData_primeiro_uso(Date data_primeiro_uso) { this.data_primeiro_uso = data_primeiro_uso; }
    public void setData_cadastro(Date data_cadastro) { this.data_cadastro = data_cadastro; }
    public void setData_atualizacao(Date data_atualizacao) { this.data_atualizacao = data_atualizacao; }

    @Override
    public String toString() {
        return "Medicamento [id_medicamento=" + id_medicamento + ", id_catalogo=" + id_catalogo + ", id_paciente=" + id_paciente + ", dosagem=" + dosagem + ", estoque_atual=" + estoque_atual + ", estoque_minimo=" + estoque_minimo + ", data_validade=" + data_validade + ", status=" + status + ", data_primeiro_uso=" + data_primeiro_uso + ", data_atualizacao=" + data_atualizacao + "]";
    }
}
