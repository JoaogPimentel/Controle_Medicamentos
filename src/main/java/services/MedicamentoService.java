package services;

import dao.AlertaDAO;
import dao.HistoricoUsoDAO;
import dao.MedicamentoDAO;
import dao.MovimentacaoEstoqueDAO;
import dao.PosologiaDAO;
import model.Alerta;
import model.HistoricoUso;
import model.Medicamento;
import model.MovimentacaoEstoque;
import model.Posologia;
import model.StatusDose;
import model.TipoAlerta;
import model.TipoMovimentacao;

import java.sql.SQLException;

public class MedicamentoService {

    private final MedicamentoDAO medicamentoDAO       = new MedicamentoDAO();
    private final PosologiaDAO posologiaDAO           = new PosologiaDAO();
    private final HistoricoUsoDAO historicoDAO        = new HistoricoUsoDAO();
    private final MovimentacaoEstoqueDAO movimentacaoDAO = new MovimentacaoEstoqueDAO();
    private final AlertaDAO alertaDAO                 = new AlertaDAO();

    public Medicamento cadastrar(Medicamento medicamento, double quantidadeInicial, int idResponsavel)
            throws SQLException {
        medicamentoDAO.insert(medicamento);

        if (quantidadeInicial > 0) {
            MovimentacaoEstoque mov = new MovimentacaoEstoque();
            mov.setId_medicamento(medicamento.getId_medicamento());
            mov.setId_registrado_por(idResponsavel);
            mov.setTipo(TipoMovimentacao.ENTRADA_COMPRA);
            mov.setQuantidade(quantidadeInicial);
            mov.setEstoque_antes(0.0);
            mov.setEstoque_depois(quantidadeInicial);
            movimentacaoDAO.insert(mov);
        }

        if (quantidadeInicial <= medicamento.getEstoque_minimo()) {
            gerarAlerta(medicamento, TipoAlerta.ESTOQUE_BAIXO, "Estoque do medicamento está abaixo do mínimo.");
        }

        return medicamento;
    }

    public void registrarDose(int idPosologia, int idResponsavel, String observacao) throws SQLException {
        Posologia posologia = posologiaDAO.findById(idPosologia);
        if (posologia == null || !posologia.getAtivo()) {
            throw new IllegalArgumentException("Posologia não encontrada ou inativa.");
        }

        Medicamento medicamento = medicamentoDAO.findById(posologia.getId_medicamento());
        if (medicamento == null) {
            throw new IllegalArgumentException("Medicamento não encontrado.");
        }

        HistoricoUso historico = new HistoricoUso();
        historico.setId_posologia(idPosologia);
        historico.setData_hora(new java.util.Date());
        historico.setStatus(StatusDose.TOMADA);
        historico.setId_registrado_por(idResponsavel);
        historico.setObservacao(observacao);
        historicoDAO.insert(historico);

        double estoqueAntes  = medicamento.getEstoque_atual();
        double estoqueDepois = estoqueAntes - posologia.getQuantidade_por_dose();

        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setId_medicamento(medicamento.getId_medicamento());
        mov.setId_registrado_por(idResponsavel);
        mov.setTipo(TipoMovimentacao.SAIDA_DOSE);
        mov.setQuantidade(posologia.getQuantidade_por_dose());
        mov.setEstoque_antes(estoqueAntes);
        mov.setEstoque_depois(estoqueDepois);
        mov.setId_historico_uso(historico.getId_historico());
        movimentacaoDAO.insert(mov);

        if (estoqueDepois <= 0.0) {
            gerarAlerta(medicamento, TipoAlerta.ESTOQUE_ZERADO, "Estoque do medicamento zerou.");
        } else if (estoqueDepois <= medicamento.getEstoque_minimo()) {
            gerarAlerta(medicamento, TipoAlerta.ESTOQUE_BAIXO, "Estoque do medicamento está abaixo do mínimo.");
        }
    }

    public Posologia iniciarTratamento(Posologia posologia) throws SQLException {
        posologiaDAO.insert(posologia);
        // trigger no banco transiciona o status do medicamento para EM_USO automaticamente
        return posologia;
    }

    private void gerarAlerta(Medicamento medicamento, TipoAlerta tipo, String mensagem) throws SQLException {
        Alerta alerta = new Alerta();
        alerta.setId_pessoa(medicamento.getId_paciente());
        alerta.setId_medicamento(medicamento.getId_medicamento());
        alerta.setTipo(tipo);
        alerta.setMensagem(mensagem);
        alerta.setLido(false);
        alertaDAO.insert(alerta);
    }
}
