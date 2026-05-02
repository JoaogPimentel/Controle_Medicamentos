package services;

import dao.AlertaDAO;
import dao.MedicamentoDAO;
import model.Alerta;
import model.Medicamento;
import model.StatusMedicamento;
import model.TipoAlerta;

import java.sql.SQLException;
import java.util.List;

public class AlertaService {

    private final MedicamentoDAO medicamentoDAO = new MedicamentoDAO();
    private final AlertaDAO alertaDAO           = new AlertaDAO();

    private static final long TRINTA_DIAS_MS = 30L * 24 * 60 * 60 * 1000;

    public void verificarAlertas(int idPaciente) throws SQLException {
        List<Medicamento> medicamentos = medicamentoDAO.findByPaciente(idPaciente);
        long agora = System.currentTimeMillis();

        for (Medicamento m : medicamentos) {
            if (m.getStatus() == StatusMedicamento.DESCARTADO
                    || m.getStatus() == StatusMedicamento.ARQUIVADO) {
                continue;
            }

            if (m.getData_validade() != null) {
                long validade = m.getData_validade().getTime();
                if (validade > agora && validade <= agora + TRINTA_DIAS_MS) {
                    gerarAlerta(m, TipoAlerta.VENCIMENTO_PROXIMO, "Medicamento vence em menos de 30 dias.");
                }
            }

            if (m.getEstoque_atual() <= 0.0) {
                gerarAlerta(m, TipoAlerta.ESTOQUE_ZERADO, "Estoque do medicamento zerou.");
            } else if (m.getEstoque_atual() <= m.getEstoque_minimo()) {
                gerarAlerta(m, TipoAlerta.ESTOQUE_BAIXO, "Estoque do medicamento está abaixo do mínimo.");
            }
        }
    }

    public List<Alerta> listarNaoLidos(int idPessoa) throws SQLException {
        return alertaDAO.findNaoLidosByPessoa(idPessoa);
    }

    public void marcarLido(int idAlerta) throws SQLException {
        if (alertaDAO.findById(idAlerta) == null) {
            throw new IllegalArgumentException("Alerta não encontrado.");
        }
        alertaDAO.marcarLido(idAlerta);
    }

    private void gerarAlerta(Medicamento m, TipoAlerta tipo, String mensagem) throws SQLException {
        Alerta alerta = new Alerta();
        alerta.setId_pessoa(m.getId_paciente());
        alerta.setId_medicamento(m.getId_medicamento());
        alerta.setTipo(tipo);
        alerta.setMensagem(mensagem);
        alerta.setLido(false);
        alertaDAO.insert(alerta);
    }
}
