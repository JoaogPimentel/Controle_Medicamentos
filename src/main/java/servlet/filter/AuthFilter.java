package servlet.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.UsuarioSessao;
import utils.JsonUtil;

import java.io.IOException;
import java.util.Set;

public class AuthFilter implements Filter {

    /**
     * Rotas que não exigem autenticação.
     * Tudo fora desta lista precisa de sessão válida.
     */
    private static final Set<String> ROTAS_PUBLICAS = Set.of(
        "/login",
        "/api/auth/login",
        "/api/auth/cadastrar"
    );

    @Override
    public void init(FilterConfig config) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String caminho = req.getRequestURI()
                           .substring(req.getContextPath().length());

        // Rotas públicas e arquivos estáticos passam sem verificação
        if (ehPublico(caminho)) {
            aplicarCacheEstatico(caminho, resp);
            chain.doFilter(request, response);
            return;
        }

        // Página dinâmica: nunca deve ser cacheada pelo browser
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        // Verifica sessão
        HttpSession    sessao  = req.getSession(false);
        UsuarioSessao  usuario = (sessao != null)
                                 ? (UsuarioSessao) sessao.getAttribute("usuario")
                                 : null;

        if (usuario == null) {
            if (caminho.startsWith("/api/")) {
                // Cliente de API recebe 401 JSON
                JsonUtil.send(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    JsonUtil.error("Não autenticado. Faça login em /api/auth/login."));
            } else {
                // Navegador é redirecionado para a tela de login
                resp.sendRedirect(req.getContextPath() + "/login");
            }
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}

    private boolean ehPublico(String caminho) {
        return ROTAS_PUBLICAS.contains(caminho)
            || caminho.startsWith("/css/")
            || caminho.startsWith("/js/")
            || caminho.startsWith("/img/")
            || caminho.equals("/")
            || caminho.isEmpty();
    }

    /** Arquivos estáticos podem ser cacheados por 1 dia. */
    private void aplicarCacheEstatico(String caminho, HttpServletResponse resp) {
        if (caminho.startsWith("/css/") || caminho.startsWith("/js/") || caminho.startsWith("/img/")) {
            resp.setHeader("Cache-Control", "public, max-age=86400");
        }
    }
}
