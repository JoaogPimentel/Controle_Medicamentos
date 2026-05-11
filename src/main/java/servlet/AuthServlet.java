package servlet;

import dao.RoleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Pessoa;
import model.RolePessoa;
import model.UsuarioSessao;
import services.AuthService;
import utils.JsonUtil;

import java.io.IOException;
import java.sql.Date;

public class AuthServlet extends HttpServlet {

    private static final int SESSAO_TIMEOUT_SEGUNDOS = 30 * 60;
    private static final int COOKIE_MAX_AGE_SEGUNDOS = 30 * 24 * 3600;
    public static final String COOKIE_EMAIL = "lembrar_email";
    public static final String ATTR_USUARIO = "usuario";

    private final AuthService authService = new AuthService();

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

        HttpSession sessao = req.getSession(true);
        sessao.setMaxInactiveInterval(SESSAO_TIMEOUT_SEGUNDOS);

        RolePessoa papel = new RoleDAO().findRole(pessoa.getId_pessoa());
        UsuarioSessao usuario = new UsuarioSessao(
            pessoa.getId_pessoa(), pessoa.getNome(), pessoa.getEmail(), papel);
        sessao.setAttribute(ATTR_USUARIO, usuario);

        String lembrar = JsonUtil.getString(body, "lembrar");
        if ("true".equals(lembrar)) {
            Cookie cookie = new Cookie(COOKIE_EMAIL, email);
            cookie.setMaxAge(COOKIE_MAX_AGE_SEGUNDOS);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            resp.addCookie(cookie);
        }

        JsonUtil.send(resp, HttpServletResponse.SC_OK, pessoaToJson(pessoa, papel));
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
        HttpSession sessao = req.getSession(false);
        if (sessao != null) sessao.invalidate();

        Cookie cookie = new Cookie(COOKIE_EMAIL, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        resp.addCookie(cookie);

        resp.sendRedirect(req.getContextPath() + "/login");
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
