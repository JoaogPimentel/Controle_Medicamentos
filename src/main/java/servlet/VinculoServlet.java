package servlet;

import dao.PacienteDAO;
import dao.PessoaDAO;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Pessoa;
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
            String path = req.getPathInfo();

            if ("/buscar-paciente".equals(path)) {
                buscarPaciente(req, resp);
                return;
            }

            String pacienteParam = req.getParameter("paciente");
            String cuidadorParam = req.getParameter("cuidador");

            List<model.PacienteCuidador> lista;
            boolean incluirNome = false;
            if (pacienteParam != null) {
                lista = service.listarPorPaciente(Integer.parseInt(pacienteParam));
            } else if (cuidadorParam != null) {
                lista = service.listarPorCuidador(Integer.parseInt(cuidadorParam));
                incluirNome = true;
            } else {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST,
                        JsonUtil.error("Parâmetro 'paciente' ou 'cuidador' obrigatório."));
                return;
            }

            PessoaDAO pessoaDAO = incluirNome ? new PessoaDAO() : null;
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                if (i > 0) json.append(",");
                model.PacienteCuidador v = lista.get(i);
                String nomePaciente = null;
                if (pessoaDAO != null) {
                    Pessoa p = pessoaDAO.findById(v.getId_paciente());
                    nomePaciente = p != null ? p.getNome() : null;
                }
                json.append(vinculoToJson(v, nomePaciente));
            }
            json.append("]");
            JsonUtil.send(resp, HttpServletResponse.SC_OK, json.toString());
        } catch (NumberFormatException e) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("ID inválido."));
        } catch (Exception e) {
            JsonUtil.send(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonUtil.error("Erro interno no servidor."));
        }
    }

    private void buscarPaciente(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String email = req.getParameter("email");
        if (email == null || email.isBlank()) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST,
                JsonUtil.error("Parâmetro 'email' obrigatório."));
            return;
        }

        Pessoa pessoa = new PessoaDAO().findByEmail(email);
        if (pessoa == null) {
            JsonUtil.send(resp, HttpServletResponse.SC_NOT_FOUND,
                JsonUtil.error("Nenhum usuário encontrado com este e-mail."));
            return;
        }

        if (new PacienteDAO().findById(pessoa.getId_pessoa()) == null) {
            JsonUtil.send(resp, HttpServletResponse.SC_NOT_FOUND,
                JsonUtil.error("Este usuário não está cadastrado como paciente."));
            return;
        }

        String json = "{\"id_pessoa\":" + pessoa.getId_pessoa() +
                      ",\"nome\":\"" + JsonUtil.escape(pessoa.getNome()) + "\"}";
        JsonUtil.send(resp, HttpServletResponse.SC_OK, json);
    }

    private boolean isCuidador(HttpServletRequest req) {
        var sessao = req.getSession(false);
        if (sessao == null) return false;
        UsuarioSessao usuario = (UsuarioSessao) sessao.getAttribute(AuthServlet.ATTR_USUARIO);
        return usuario != null && usuario.getPapel() == RolePessoa.CUIDADOR;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!isCuidador(req)) {
            JsonUtil.send(resp, HttpServletResponse.SC_FORBIDDEN,
                JsonUtil.error("Acesso negado. Apenas cuidadores podem gerenciar vínculos."));
            return;
        }

        try {
            UsuarioSessao usuario = (UsuarioSessao) req.getSession(false).getAttribute(AuthServlet.ATTR_USUARIO);
            String body        = JsonUtil.readBody(req);
            Integer idPaciente = JsonUtil.getInt(body, "id_paciente");
            int idCuidador     = usuario.getIdPessoa();

            if (idPaciente == null) {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("Campo obrigatório: id_paciente."));
                return;
            }

            PacienteCuidador vinculo = service.vincular(idPaciente, idCuidador);
            JsonUtil.send(resp, HttpServletResponse.SC_CREATED, vinculoToJson(vinculo, null));
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

    private String vinculoToJson(PacienteCuidador v, String nomePaciente) {
        String nome = nomePaciente != null ? nomePaciente : "Paciente #" + v.getId_paciente();
        return "{"
            + "\"id_vinculo\":"      + v.getId_vinculo()             + ","
            + "\"id_paciente\":"     + v.getId_paciente()            + ","
            + "\"nome_paciente\":\"" + JsonUtil.escape(nome)         + "\","
            + "\"id_cuidador\":"     + v.getId_cuidador()            + ","
            + "\"ativo\":"           + v.isAtivo()
            + "}";
    }
}
