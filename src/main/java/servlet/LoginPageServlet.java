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

import java.io.IOException;

public class LoginPageServlet extends HttpServlet {

    private static final int SESSAO_TIMEOUT   = 30 * 60;
    private static final int COOKIE_MAX_AGE   = 30 * 24 * 3600;

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        HttpSession sessaoExistente = req.getSession(false);
        if (sessaoExistente != null && sessaoExistente.getAttribute(AuthServlet.ATTR_USUARIO) != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        String emailCookie = "";
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (AuthServlet.COOKIE_EMAIL.equals(c.getName())) {
                    emailCookie = c.getValue();
                    break;
                }
            }
        }

        req.setAttribute("emailPreenchido", emailCookie);
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String email  = req.getParameter("email");
        String senha  = req.getParameter("senha");
        String lembrar = req.getParameter("lembrar");

        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            req.setAttribute("erro", "E-mail e senha são obrigatórios.");
            req.setAttribute("emailPreenchido", email != null ? email : "");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        try {
            Pessoa pessoa = authService.login(email, senha);

            HttpSession sessao = req.getSession(true);
            sessao.setMaxInactiveInterval(SESSAO_TIMEOUT);

            RolePessoa papel = new RoleDAO().findRole(pessoa.getId_pessoa());
            sessao.setAttribute(AuthServlet.ATTR_USUARIO,
                new UsuarioSessao(pessoa.getId_pessoa(), pessoa.getNome(), pessoa.getEmail(), papel));

            if ("on".equals(lembrar)) {
                Cookie cookie = new Cookie(AuthServlet.COOKIE_EMAIL, email);
                cookie.setMaxAge(COOKIE_MAX_AGE);
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                resp.addCookie(cookie);
            } else {
                Cookie cookie = new Cookie(AuthServlet.COOKIE_EMAIL, "");
                cookie.setMaxAge(0);
                cookie.setPath("/");
                resp.addCookie(cookie);
            }

            resp.sendRedirect(req.getContextPath() + "/dashboard");

        } catch (IllegalArgumentException e) {
            req.setAttribute("erro", e.getMessage());
            req.setAttribute("emailPreenchido", email);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("erro", "Erro interno. Tente novamente.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }
}
