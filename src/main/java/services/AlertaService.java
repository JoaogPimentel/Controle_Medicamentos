package services;

import dao.AlertaDAO;
import dao.MedicamentoDAO;
import dao.PosologiaDAO;
import model.Alerta;
import model.Medicamento;
import model.Posologia;
import model.StatusMedicamento;
import model.TipoAlerta;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class AlertaService {

    private final MedicamentoDAO medicamentoDAO = new MedicamentoDAO();
    private final AlertaDAO alertaDAO           = new AlertaDAO();
    private final PosologiaDAO posologiaDAO     = new PosologiaDAO();

    private static final long TRINTA_DIAS_MS = 30L * 24 * 60 * 60 * 1000;

    public void verificarAlertas(int idPaciente) throws SQLException {
        verificarDoses(idPaciente);
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

            double estoqueAtual  = m.getEstoque_atual()  != null ? m.getEstoque_atual()  : 0.0;
            double estoqueMinimo = m.getEstoque_minimo() != null ? m.getEstoque_minimo() : 0.0;
            if (estoqueAtual <= 0.0) {
                gerarAlerta(m, TipoAlerta.ESTOQUE_ZERADO, "Estoque do medicamento zerou.");
            } else if (estoqueAtual <= estoqueMinimo) {
                gerarAlerta(m, TipoAlerta.ESTOQUE_BAIXO, "Estoque do medicamento está abaixo do mínimo.");
            }
        }
    }

    private void verificarDoses(int idPaciente) throws SQLException {
        List<Posologia> posologias = posologiaDAO.findAtivasByPaciente(idPaciente);
        LocalDateTime agora = LocalDateTime.now();

        for (Posologia p : posologias) {
            if (p.getData_inicio() == null || p.getHorario_primeira_dose() == null) continue;
            if (p.getIntervalo_horas() == null || p.getIntervalo_horas() <= 0) continue;

            LocalDateTime primeiraDose = new java.sql.Date(p.getData_inicio().getTime())
                    .toLocalDate()
                    .atTime(p.getHorario_primeira_dose().toLocalTime());

            long minutosDecorridos = ChronoUnit.MINUTES.between(primeiraDose, agora);
            if (minutosDecorridos < 0) continue;

            long intervalMinutos = p.getIntervalo_horas() * 60L;
            long intervalosPassed = minutosDecorridos / intervalMinutos;
            LocalDateTime ultimaEsperada = primeiraDose.plusMinutes(intervalosPassed * intervalMinutos);
            long minutosTarde = ChronoUnit.MINUTES.between(ultimaEsperada, agora);

            if (minutosTarde < 0 || minutosTarde > intervalMinutos) continue;

            TipoAlerta tipo = minutosTarde <= 30 ? TipoAlerta.DOSE_PROXIMA : TipoAlerta.DOSE_ATRASADA;

            if (!alertaDAO.existeAlertaRecente(p.getId_medicamento(), tipo, p.getIntervalo_horas())) {
                Alerta a = new Alerta();
                a.setId_pessoa(idPaciente);
                a.setId_medicamento(p.getId_medicamento());
                a.setTipo(tipo);
                a.setMensagem(tipo == TipoAlerta.DOSE_PROXIMA
                        ? "Hora de tomar a dose!"
                        : "Dose atrasada! Deveria ter sido tomada às " + ultimaEsperada.toLocalTime().withSecond(0).toString() + ".");
                a.setLido(false);
                alertaDAO.insert(a);
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
        if (alertaDAO.existeAlertaRecente(m.getId_medicamento(), tipo, 24)) return;
        Alerta alerta = new Alerta();
        alerta.setId_pessoa(m.getId_paciente());
        alerta.setId_medicamento(m.getId_medicamento());
        alerta.setTipo(tipo);
        alerta.setMensagem(mensagem);
        alerta.setLido(false);
        alertaDAO.insert(alerta);
    }
}
