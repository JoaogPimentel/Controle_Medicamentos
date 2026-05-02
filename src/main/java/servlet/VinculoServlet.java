package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.PacienteCuidador;
import model.RolePessoa;
import model.UsuarioSessao;
import services.VinculoService;
import utils.JsonUtil;

import java.io.IOException;

public class VinculoServlet extends HttpServlet {

    private final VinculoService service = new VinculoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String pacienteParam = req.getParameter("paciente");
            String cuidadorParam = req.getParameter("cuidador");

            java.util.List<model.PacienteCuidador> lista;
            if (pacienteParam != null) {
                lista = service.listarPorPaciente(Integer.parseInt(pacienteParam));
            } else if (cuidadorParam != null) {
                lista = service.listarPorCuidador(Integer.parseInt(cuidadorParam));
            } else {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST,
                        JsonUtil.error("Parâmetro 'paciente' ou 'cuidador' obrigatório."));
                return;
            }

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                if (i > 0) json.append(",");
                json.append(vinculoToJson(lista.get(i)));
            }
            json.append("]");
            JsonUtil.send(resp, HttpServletResponse.SC_OK, json.toString());
        } catch (NumberFormatException e) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("ID inválido."));
        } catch (Exception e) {
            JsonUtil.send(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonUtil.error("Erro interno no servidor."));
        }
    }

    /** Verifica se o usuário logado possui o papel de CUIDADOR. */
    private boolean isCuidador(HttpServletRequest req) {
        var sessao = req.getSession(false);
        if (sessao == null) return false;
        UsuarioSessao usuario = (UsuarioSessao) sessao.getAttribute(AuthServlet.ATTR_USUARIO);
        return usuario != null && usuario.getPapel() == RolePessoa.CUIDADOR;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Apenas cuidadores podem criar vínculos
        if (!isCuidador(req)) {
            JsonUtil.send(resp, HttpServletResponse.SC_FORBIDDEN,
                JsonUtil.error("Acesso negado. Apenas cuidadores podem gerenciar vínculos."));
            return;
        }

        try {
            String body       = JsonUtil.readBody(req);
            Integer idPaciente = JsonUtil.getInt(body, "id_paciente");
            Integer idCuidador = JsonUtil.getInt(body, "id_cuidador");

            if (idPaciente == null || idCuidador == null) {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("Campos obrigatórios: id_paciente, id_cuidador."));
                return;
            }

            PacienteCuidador vinculo = service.vincular(idPaciente, idCuidador);
            JsonUtil.send(resp, HttpServletResponse.SC_CREATED, vinculoToJson(vinculo));
        } catch (IllegalArgumentException e) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            JsonUtil.send(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonUtil.error("Erro interno no servidor."));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!isCuidador(req)) {
            JsonUtil.send(resp, HttpServletResponse.SC_FORBIDDEN,
                JsonUtil.error("Acesso negado. Apenas cuidadores podem encerrar vínculos."));
            return;
        }

        try {
            // /api/vinculos/{id}
            String path = req.getPathInfo();
            if (path == null || path.equals("/")) {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("ID do vínculo não informado."));
                return;
            }

            int idVinculo = Integer.parseInt(path.replace("/", ""));
            service.desvincular(idVinculo);
            JsonUtil.send(resp, HttpServletResponse.SC_OK, JsonUtil.success("Vínculo encerrado com sucesso."));
        } catch (IllegalArgumentException e) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            JsonUtil.send(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonUtil.error("Erro interno no servidor."));
        }
    }

    private String vinculoToJson(PacienteCuidador v) {
        return "{"
            + "\"id_vinculo\":"  + v.getId_vinculo()  + ","
            + "\"id_paciente\":" + v.getId_paciente() + ","
            + "\"id_cuidador\":" + v.getId_cuidador() + ","
            + "\"ativo\":"       + v.isAtivo()
            + "}";
    }
}
