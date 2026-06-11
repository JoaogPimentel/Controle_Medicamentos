package servlet;

import dao.PacienteCuidadorDAO;
import services.AlertaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Alerta;
import model.Pessoa;
import model.RolePessoa;
import model.UsuarioSessao;
import utils.JsonUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Resumo do dashboard como API JSON ({@code GET /api/dashboard}).
 *
 * O usuário é lido do token (publicado no request pelo AuthFilter). A resposta
 * varia conforme o papel:
 * <ul>
 *   <li>PACIENTE: lista de alertas não lidos.</li>
 *   <li>CUIDADOR: pacientes vinculados com a contagem de alertas não lidos.</li>
 * </ul>
 */
public class DashboardServlet extends HttpServlet {

    private final AlertaService alertaService = new AlertaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            UsuarioSessao usuario = AuthServlet.usuarioAtual(req);
            if (usuario == null) {
                JsonUtil.send(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    JsonUtil.error("Não autenticado."));
                return;
            }

            String json = usuario.getPapel() == RolePessoa.CUIDADOR
                ? resumoCuidador(usuario.getIdPessoa())
                : resumoPaciente(usuario.getIdPessoa());

            JsonUtil.send(resp, HttpServletResponse.SC_OK, json);
        } catch (Exception e) {
            JsonUtil.send(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                JsonUtil.error("Erro interno no servidor."));
        }
    }

    private String resumoPaciente(int idPessoa) throws Exception {
        alertaService.verificarAlertas(idPessoa);
        List<Alerta> alertas = alertaService.listarNaoLidos(idPessoa);

        StringBuilder arr = new StringBuilder("[");
        for (int i = 0; i < alertas.size(); i++) {
            if (i > 0) arr.append(",");
            arr.append(alertaToJson(alertas.get(i)));
        }
        arr.append("]");

        return "{\"papel\":\"PACIENTE\",\"alertas\":" + arr + "}";
    }

    private String resumoCuidador(int idCuidador) throws Exception {
        List<Pessoa> pacientes;
        try {
            pacientes = new PacienteCuidadorDAO().findPacientesDoCuidador(idCuidador);
        } catch (Exception e) {
            pacientes = Collections.emptyList();
        }

        StringBuilder arr = new StringBuilder("[");
        for (int i = 0; i < pacientes.size(); i++) {
            Pessoa p = pacientes.get(i);
            alertaService.verificarAlertas(p.getId_pessoa());
            int naoLidos = alertaService.listarNaoLidos(p.getId_pessoa()).size();
            if (i > 0) arr.append(",");
            arr.append("{")
               .append("\"id_pessoa\":").append(p.getId_pessoa()).append(",")
               .append("\"nome\":\"").append(JsonUtil.escape(p.getNome())).append("\",")
               .append("\"alertas_nao_lidos\":").append(naoLidos)
               .append("}");
        }
        arr.append("]");

        return "{\"papel\":\"CUIDADOR\",\"pacientes\":" + arr + "}";
    }

    private String alertaToJson(Alerta a) {
        return "{"
            + "\"id_alerta\":"      + a.getId_alerta()      + ","
            + "\"id_pessoa\":"      + a.getId_pessoa()      + ","
            + "\"id_medicamento\":" + a.getId_medicamento() + ","
            + "\"tipo\":\""         + a.getTipo()           + "\","
            + "\"mensagem\":\""     + JsonUtil.escape(a.getMensagem()) + "\","
            + "\"lido\":"           + a.getLido()           + ","
            + "\"data_geracao\":\"" + a.getData_geracao()   + "\""
            + "}";
    }
}
