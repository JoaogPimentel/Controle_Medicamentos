package servlet;

import dao.AlertaDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Alerta;
import model.UsuarioSessao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Controller MVC do dashboard principal.
 *
 * GET /dashboard → carrega dados do usuário logado,
 *                  coloca como atributos da requisição e
 *                  delega a renderização para dashboard.jsp.
 */
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Página dinâmica autenticada: não deve ser cacheada
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        // O AuthFilter já garantiu que a sessão existe, mas usamos getSession(false)
        // para deixar claro que não criamos uma nova sessão aqui
        UsuarioSessao usuario = (UsuarioSessao)
            req.getSession(false).getAttribute(AuthServlet.ATTR_USUARIO);

        // Carrega alertas não lidos do usuário
        List<Alerta> alertas;
        try {
            alertas = new AlertaDAO().findNaoLidosByPessoa(usuario.getIdPessoa());
        } catch (SQLException e) {
            alertas = Collections.emptyList();
        }

        // Disponibiliza os dados para a View via atributos da requisição
        req.setAttribute("usuario", usuario);
        req.setAttribute("alertas", alertas);

        // Forward: Servlet entrega o controle de renderização para a JSP
        req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
    }
}
