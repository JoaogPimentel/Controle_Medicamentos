package servlet.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.UsuarioSessao;
import utils.CorsConfig;
import utils.JsonUtil;
import utils.JwtUtil;

import java.io.IOException;
import java.util.Set;

public class AuthFilter implements Filter {

    private static final Set<String> ROTAS_PUBLICAS = Set.of(
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

        // CORS: ecoa a origem permitida em toda resposta e responde o preflight
        // OPTIONS antes da checagem de auth (o browser não envia token no preflight).
        CorsConfig.aplicar(req, resp);
        if (CorsConfig.isPreflight(req)) {
            CorsConfig.aplicarPreflight(resp);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        String caminho = req.getRequestURI()
                           .substring(req.getContextPath().length());

        if (ehPublico(caminho)) {
            aplicarCacheEstatico(caminho, resp);
            chain.doFilter(request, response);
            return;
        }

        // Página dinâmica: nunca deve ser cacheada pelo browser
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        UsuarioSessao usuario = JwtUtil.validar(extrairToken(req));

        if (usuario == null) {
            // Back-end é uma API pura: sem token válido, responde 401 JSON
            // (não há mais página de login para redirecionar).
            JsonUtil.send(resp, HttpServletResponse.SC_UNAUTHORIZED,
                JsonUtil.error("Não autenticado. Faça login em /api/auth/login."));
            return;
        }

        // Publica o usuário do token no request para os servlets consumirem.
        req.setAttribute(servlet.AuthServlet.ATTR_USUARIO, usuario);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}

    /** Extrai o token do header {@code Authorization: Bearer <token>}; null se ausente. */
    private String extrairToken(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header == null) return null;
        header = header.trim();
        if (header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String token = header.substring(7).trim();
            return token.isEmpty() ? null : token;
        }
        return null;
    }

    private boolean ehPublico(String caminho) {
        return ROTAS_PUBLICAS.contains(caminho)
            || caminho.startsWith("/css/")
            || caminho.startsWith("/js/")
            || caminho.startsWith("/img/")
            || caminho.equals("/")
            || caminho.isEmpty();
    }

    private void aplicarCacheEstatico(String caminho, HttpServletResponse resp) {
        if (caminho.startsWith("/css/") || caminho.startsWith("/js/") || caminho.startsWith("/img/")) {
            resp.setHeader("Cache-Control", "public, max-age=86400");
        }
    }
}
