package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Medicamento;
import model.Posologia;
import model.StatusMedicamento;
import model.UsuarioSessao;
import services.MedicamentoService;
import utils.JsonUtil;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

public class MedicamentoServlet extends HttpServlet {

    private final MedicamentoService service = new MedicamentoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String path = req.getPathInfo();
            // GET /api/medicamentos/{id}
            if (path != null && !path.equals("/")) {
                int id = Integer.parseInt(path.substring(1));
                Medicamento m = new dao.MedicamentoDAO().findById(id);
                if (m == null) {
                    JsonUtil.send(resp, HttpServletResponse.SC_NOT_FOUND, JsonUtil.error("Medicamento não encontrado."));
                    return;
                }
                JsonUtil.send(resp, HttpServletResponse.SC_OK, medicamentoToJson(m));
                return;
            }

            // GET /api/medicamentos?paciente={id}
            String idParam = req.getParameter("paciente");
            if (idParam == null) {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("Parâmetro 'paciente' obrigatório."));
                return;
            }
            int idPaciente = Integer.parseInt(idParam);
            List<Medicamento> lista = new dao.MedicamentoDAO().findByPaciente(idPaciente);

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                if (i > 0) json.append(",");
                json.append(medicamentoToJson(lista.get(i)));
            }
            json.append("]");
            JsonUtil.send(resp, HttpServletResponse.SC_OK, json.toString());
        } catch (NumberFormatException e) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("ID inválido."));
        } catch (Exception e) {
            JsonUtil.send(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonUtil.error("Erro interno no servidor."));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String path = req.getPathInfo();
            if (path == null || path.equals("/")) {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("ID do medicamento não informado."));
                return;
            }
            int id = Integer.parseInt(path.substring(1));
            dao.MedicamentoDAO medicamentoDAO = new dao.MedicamentoDAO();
            Medicamento m = medicamentoDAO.findById(id);
            if (m == null) {
                JsonUtil.send(resp, HttpServletResponse.SC_NOT_FOUND, JsonUtil.error("Medicamento não encontrado."));
                return;
            }

            String body = JsonUtil.readBody(req);
            String dosagem = JsonUtil.getString(body, "dosagem");
            Double estoqueMinimo = JsonUtil.getDouble(body, "estoque_minimo");
            String dataValidade = JsonUtil.getString(body, "data_validade");
            String statusStr = JsonUtil.getString(body, "status");

            if (dosagem != null) m.setDosagem(dosagem);
            if (estoqueMinimo != null) m.setEstoque_minimo(estoqueMinimo);
            if (dataValidade != null) m.setData_validade(Date.valueOf(dataValidade));
            if (statusStr != null) {
                try {
                    m.setStatus(StatusMedicamento.valueOf(statusStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("Status inválido."));
                    return;
                }
            }

            medicamentoDAO.update(m);
            JsonUtil.send(resp, HttpServletResponse.SC_OK, medicamentoToJson(m));
        } catch (NumberFormatException e) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("ID inválido."));
        } catch (Exception e) {
            JsonUtil.send(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonUtil.error("Erro interno no servidor."));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String path = req.getPathInfo();
            if (path == null || path.equals("/")) {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("ID do medicamento não informado."));
                return;
            }
            int id = Integer.parseInt(path.substring(1));
            dao.MedicamentoDAO medicamentoDAO = new dao.MedicamentoDAO();
            Medicamento m = medicamentoDAO.findById(id);
            if (m == null) {
                JsonUtil.send(resp, HttpServletResponse.SC_NOT_FOUND, JsonUtil.error("Medicamento não encontrado."));
                return;
            }
            if ("true".equals(req.getParameter("force"))) {
                medicamentoDAO.delete(id);
                JsonUtil.send(resp, HttpServletResponse.SC_OK, JsonUtil.success("Medicamento excluído permanentemente."));
            } else {
                m.setStatus(StatusMedicamento.ARQUIVADO);
                medicamentoDAO.update(m);
                JsonUtil.send(resp, HttpServletResponse.SC_OK, JsonUtil.success("Medicamento arquivado com sucesso."));
            }
        } catch (NumberFormatException e) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("ID inválido."));
        } catch (Exception e) {
            JsonUtil.send(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonUtil.error("Erro interno no servidor."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();

        try {
            if ("/dose".equals(path)) {
                registrarDose(req, resp);
            } else if ("/tratamento".equals(path)) {
                iniciarTratamento(req, resp);
            } else {
                cadastrar(req, resp);
            }
        } catch (IllegalArgumentException e) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            JsonUtil.send(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonUtil.error("Erro interno no servidor."));
        }
    }

    private void cadastrar(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        UsuarioSessao usuario = AuthServlet.usuarioAtual(req);
        String body = JsonUtil.readBody(req);

        Medicamento m = new Medicamento();
        m.setId_paciente(JsonUtil.getInt(body, "id_paciente"));
        m.setId_catalogo(JsonUtil.getInt(body, "id_catalogo"));
        m.setId_cadastrado_por(usuario.getIdPessoa());
        m.setDosagem(JsonUtil.getString(body, "dosagem"));
        m.setEstoque_minimo(JsonUtil.getDouble(body, "estoque_minimo") != null ? JsonUtil.getDouble(body, "estoque_minimo") : 0.0);
        m.setStatus(StatusMedicamento.EM_ESTOQUE);

        String dataValidade = JsonUtil.getString(body, "data_validade");
        if (dataValidade != null) {
            Date dataSql;
            try {
                dataSql = Date.valueOf(dataValidade);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Data de validade inválida. Use o formato AAAA-MM-DD.");
            }
            if (!dataSql.toLocalDate().isAfter(java.time.LocalDate.now())) {
                throw new IllegalArgumentException("A data de validade deve ser posterior à data de hoje.");
            }
            m.setData_validade(dataSql);
        }

        Double qtdInicial = JsonUtil.getDouble(body, "quantidade_inicial");

        if (m.getId_paciente() == null || m.getId_catalogo() == null || m.getDosagem() == null) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("Campos obrigatórios: id_paciente, id_catalogo, dosagem."));
            return;
        }

        service.cadastrar(m, qtdInicial != null ? qtdInicial : 0.0, usuario.getIdPessoa());
        JsonUtil.send(resp, HttpServletResponse.SC_CREATED, medicamentoToJson(m));
    }

    private void registrarDose(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        UsuarioSessao usuario = AuthServlet.usuarioAtual(req);
        String body          = JsonUtil.readBody(req);
        Integer idPosologia  = JsonUtil.getInt(body, "id_posologia");
        String observacao    = JsonUtil.getString(body, "observacao");

        if (idPosologia == null) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("Campo obrigatório: id_posologia."));
            return;
        }

        service.registrarDose(idPosologia, usuario.getIdPessoa(), observacao);
        JsonUtil.send(resp, HttpServletResponse.SC_OK, JsonUtil.success("Dose registrada com sucesso."));
    }

    private void iniciarTratamento(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String body = JsonUtil.readBody(req);

        Posologia p = new Posologia();
        p.setId_medicamento(JsonUtil.getInt(body, "id_medicamento"));
        p.setIntervalo_horas(JsonUtil.getInt(body, "intervalo_horas"));
        p.setDuracao_dias(JsonUtil.getInt(body, "duracao_dias"));
        p.setQuantidade_por_dose(JsonUtil.getDouble(body, "quantidade_por_dose"));
        p.setAtivo(true);

        String horario     = JsonUtil.getString(body, "horario_primeira_dose");
        String dataInicio  = JsonUtil.getString(body, "data_inicio");

        if (p.getId_medicamento() == null || p.getIntervalo_horas() == null || p.getQuantidade_por_dose() == null || horario == null) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("Campos obrigatórios: id_medicamento, intervalo_horas, quantidade_por_dose, horario_primeira_dose."));
            return;
        }

        p.setHorario_primeira_dose(Time.valueOf(horario));
        p.setData_inicio(dataInicio != null ? Date.valueOf(dataInicio) : new java.util.Date());

        service.iniciarTratamento(p);
        JsonUtil.send(resp, HttpServletResponse.SC_CREATED, JsonUtil.success("Tratamento iniciado com sucesso."));
    }

    private String medicamentoToJson(Medicamento m) {
        return "{"
            + "\"id_medicamento\":"  + m.getId_medicamento()  + ","
            + "\"id_paciente\":"     + m.getId_paciente()     + ","
            + "\"id_catalogo\":"     + m.getId_catalogo()     + ","
            + "\"dosagem\":\""       + JsonUtil.escape(m.getDosagem()) + "\","
            + "\"estoque_atual\":"   + m.getEstoque_atual()   + ","
            + "\"estoque_minimo\":"  + m.getEstoque_minimo()  + ","
            + "\"status\":\""        + m.getStatus()          + "\","
            + "\"data_validade\":"   + (m.getData_validade() != null ? "\"" + m.getData_validade() + "\"" : "null")
            + "}";
    }
}
