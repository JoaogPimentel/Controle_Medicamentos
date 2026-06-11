package servlet;

import dao.RoleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Pessoa;
import model.RolePessoa;
import model.UsuarioSessao;
import services.AuthService;
import utils.JsonUtil;
import utils.JwtUtil;

import java.io.IOException;
import java.sql.Date;

public class AuthServlet extends HttpServlet {

    /** Nome do atributo de request onde o {@link AuthFilter} publica o usuário do token. */
    public static final String ATTR_USUARIO = "usuario";

    private final AuthService authService = new AuthService();

    /** Usuário autenticado do request atual (preenchido pelo AuthFilter a partir do JWT). */
    public static UsuarioSessao usuarioAtual(HttpServletRequest req) {
        return (UsuarioSessao) req.getAttribute(ATTR_USUARIO);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if ("/logout".equals(path)) {
            logout(req, resp);
        } else {
            JsonUtil.send(resp, HttpServletResponse.SC_NOT_FOUND,
                JsonUtil.error("Rota não encontrada."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        try {
            if ("/login".equals(path)) {
                login(req, resp);
            } else if ("/cadastrar".equals(path)) {
                cadastrar(req, resp);
            } else {
                JsonUtil.send(resp, HttpServletResponse.SC_NOT_FOUND,
                    JsonUtil.error("Rota não encontrada."));
            }
        } catch (IllegalArgumentException e) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST,
                JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.send(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                JsonUtil.error("Erro interno: " + e.getClass().getSimpleName() + " – " + e.getMessage()));
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String body  = JsonUtil.readBody(req);
        String email = JsonUtil.getString(body, "email");
        String senha = JsonUtil.getString(body, "senha");

        if (email == null || senha == null) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST,
                JsonUtil.error("Email e senha são obrigatórios."));
            return;
        }

        Pessoa pessoa = authService.login(email, senha);

        RolePessoa papel = new RoleDAO().findRole(pessoa.getId_pessoa());
        String token = JwtUtil.gerar(pessoa.getId_pessoa(), pessoa.getNome(), papel);

        String json = "{"
            + "\"token\":\"" + token + "\","
            + "\"usuario\":" + pessoaToJson(pessoa, papel)
            + "}";
        JsonUtil.send(resp, HttpServletResponse.SC_OK, json);
    }

    private void cadastrar(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String body                = JsonUtil.readBody(req);
        String email               = JsonUtil.getString(body, "email");
        String senha               = JsonUtil.getString(body, "senha");
        String nome                = JsonUtil.getString(body, "nome");
        String telefone            = JsonUtil.getString(body, "telefone");
        String dataNascimento      = JsonUtil.getString(body, "data_nascimento");
        String roleStr             = JsonUtil.getString(body, "role");
        String registroProf        = JsonUtil.getString(body, "registro_profissional");
        String profissionalStr     = JsonUtil.getString(body, "profissional_saude");

        if (email == null || senha == null || nome == null || dataNascimento == null) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST,
                JsonUtil.error("Campos obrigatórios: nome, email, senha, data_nascimento."));
            return;
        }

        RolePessoa role = "CUIDADOR".equalsIgnoreCase(roleStr) ? RolePessoa.CUIDADOR : RolePessoa.PACIENTE;
        boolean profissionalSaude = "true".equalsIgnoreCase(profissionalStr);

        Date nascimento = Date.valueOf(dataNascimento);
        Pessoa pessoa = authService.cadastrar(email, senha, nome, telefone, nascimento,
                                              role, profissionalSaude, registroProf);
        JsonUtil.send(resp, HttpServletResponse.SC_CREATED, pessoaToJson(pessoa, role));
    }

    private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Auth stateless (JWT): não há sessão no servidor — o token é descartado
        // pelo cliente. A rota é mantida apenas para confirmar o logout.
        JsonUtil.send(resp, HttpServletResponse.SC_OK, JsonUtil.success("Logout efetuado."));
    }

    private String pessoaToJson(Pessoa p, RolePessoa papel) {
        return "{"
            + "\"id_pessoa\":"  + p.getId_pessoa()             + ","
            + "\"nome\":\""     + JsonUtil.escape(p.getNome()) + "\","
            + "\"email\":\""    + JsonUtil.escape(p.getEmail()) + "\","
            + "\"papel\":\""    + papel.name()                 + "\""
            + "}";
    }
}
