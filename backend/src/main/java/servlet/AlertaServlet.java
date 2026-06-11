package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Alerta;
import services.AlertaService;
import utils.JsonUtil;

import java.io.IOException;
import java.util.List;

public class AlertaServlet extends HttpServlet {

    private final AlertaService service = new AlertaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String idParam = req.getParameter("pessoa");
            if (idParam == null) {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("Parâmetro 'pessoa' obrigatório."));
                return;
            }

            int idPessoa = Integer.parseInt(idParam);
            List<Alerta> alertas = service.listarNaoLidos(idPessoa);

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < alertas.size(); i++) {
                if (i > 0) json.append(",");
                json.append(alertaToJson(alertas.get(i)));
            }
            json.append("]");
            JsonUtil.send(resp, HttpServletResponse.SC_OK, json.toString());
        } catch (Exception e) {
            JsonUtil.send(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonUtil.error("Erro interno no servidor."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            // /api/alertas/{id}/lido
            String path = req.getPathInfo();
            if (path == null || !path.endsWith("/lido")) {
                JsonUtil.send(resp, HttpServletResponse.SC_NOT_FOUND, JsonUtil.error("Rota não encontrada."));
                return;
            }

            String[] parts = path.split("/");
            if (parts.length < 3) {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("ID do alerta não informado."));
                return;
            }
            int idAlerta;
            try {
                idAlerta = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("ID inválido."));
                return;
            }
            service.marcarLido(idAlerta);
            JsonUtil.send(resp, HttpServletResponse.SC_OK, JsonUtil.success("Alerta marcado como lido."));
        } catch (IllegalArgumentException e) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            JsonUtil.send(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonUtil.error("Erro interno no servidor."));
        }
    }

    private String alertaToJson(Alerta a) {
        return "{"
            + "\"id_alerta\":"     + a.getId_alerta()    + ","
            + "\"id_pessoa\":"     + a.getId_pessoa()    + ","
            + "\"id_medicamento\":" + a.getId_medicamento() + ","
            + "\"tipo\":\""        + a.getTipo()         + "\","
            + "\"mensagem\":\""    + JsonUtil.escape(a.getMensagem()) + "\","
            + "\"lido\":"          + a.getLido()         + ","
            + "\"data_geracao\":\"" + a.getData_geracao() + "\""
            + "}";
    }
}
