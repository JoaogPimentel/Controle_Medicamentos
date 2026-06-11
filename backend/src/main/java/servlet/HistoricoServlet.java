package servlet;

import dao.HistoricoUsoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.HistoricoUso;
import utils.JsonUtil;

import java.io.IOException;
import java.util.List;

public class HistoricoServlet extends HttpServlet {

    private final HistoricoUsoDAO dao = new HistoricoUsoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String idParam = req.getParameter("posologia");
            if (idParam == null) {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("Parâmetro 'posologia' obrigatório."));
                return;
            }
            int idPosologia = Integer.parseInt(idParam);
            List<HistoricoUso> lista = dao.findByPosologia(idPosologia);

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                if (i > 0) json.append(",");
                json.append(historicoToJson(lista.get(i)));
            }
            json.append("]");
            JsonUtil.send(resp, HttpServletResponse.SC_OK, json.toString());
        } catch (NumberFormatException e) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("ID inválido."));
        } catch (Exception e) {
            JsonUtil.send(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonUtil.error("Erro interno no servidor."));
        }
    }

    private String historicoToJson(HistoricoUso h) {
        return "{"
            + "\"id_historico\":"      + h.getId_historico()   + ","
            + "\"id_posologia\":"      + h.getId_posologia()   + ","
            + "\"data_hora\":\""       + h.getData_hora()      + "\","
            + "\"status\":\""          + h.getStatus()         + "\","
            + "\"id_registrado_por\":" + h.getId_registrado_por() + ","
            + "\"observacao\":"        + (h.getObservacao() != null ? "\"" + JsonUtil.escape(h.getObservacao()) + "\"" : "null")
            + "}";
    }
}
