package servlet;

import dao.PosologiaDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Posologia;
import utils.JsonUtil;

import java.io.IOException;
import java.util.List;

public class PosologiaServlet extends HttpServlet {

    private final PosologiaDAO dao = new PosologiaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String idParam = req.getParameter("medicamento");
            if (idParam == null) {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("Parâmetro 'medicamento' obrigatório."));
                return;
            }
            int idMedicamento = Integer.parseInt(idParam);
            List<Posologia> lista = dao.findByMedicamento(idMedicamento);

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                if (i > 0) json.append(",");
                json.append(posologiaToJson(lista.get(i)));
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
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("ID da posologia não informado."));
                return;
            }
            int id = Integer.parseInt(path.substring(1));
            Posologia p = dao.findById(id);
            if (p == null) {
                JsonUtil.send(resp, HttpServletResponse.SC_NOT_FOUND, JsonUtil.error("Posologia não encontrada."));
                return;
            }
            dao.reativar(id);
            JsonUtil.send(resp, HttpServletResponse.SC_OK, JsonUtil.success("Posologia reativada com sucesso."));
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
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("ID da posologia não informado."));
                return;
            }
            int id = Integer.parseInt(path.substring(1));
            Posologia p = dao.findById(id);
            if (p == null) {
                JsonUtil.send(resp, HttpServletResponse.SC_NOT_FOUND, JsonUtil.error("Posologia não encontrada."));
                return;
            }
            dao.desativar(id);
            JsonUtil.send(resp, HttpServletResponse.SC_OK, JsonUtil.success("Posologia desativada com sucesso."));
        } catch (NumberFormatException e) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("ID inválido."));
        } catch (Exception e) {
            JsonUtil.send(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonUtil.error("Erro interno no servidor."));
        }
    }

    private String posologiaToJson(Posologia p) {
        return "{"
            + "\"id_posologia\":"        + p.getId_posologia()        + ","
            + "\"id_medicamento\":"      + p.getId_medicamento()      + ","
            + "\"horario_primeira_dose\":\"" + p.getHorario_primeira_dose() + "\","
            + "\"intervalo_horas\":"     + p.getIntervalo_horas()     + ","
            + "\"duracao_dias\":"        + p.getDuracao_dias()        + ","
            + "\"quantidade_por_dose\":" + p.getQuantidade_por_dose() + ","
            + "\"data_inicio\":\""       + p.getData_inicio()         + "\","
            + "\"ativo\":"               + p.getAtivo()
            + "}";
    }
}
