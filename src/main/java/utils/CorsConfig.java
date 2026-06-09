package utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Configuração de CORS para a API desacoplada.
 *
 * As origens permitidas vêm da variável de ambiente {@code CORS_ORIGIN}
 * (lista separada por vírgulas, ex.: {@code https://usuario.github.io,http://localhost:5173}).
 * Quando não definida, libera apenas {@code http://localhost:5173} (Vite em dev).
 *
 * Como a autenticação é via {@code Authorization: Bearer} (e não cookies), não é
 * necessário {@code Access-Control-Allow-Credentials} — por isso ecoamos a origem
 * exata em vez de usar {@code *}, sem expor credenciais.
 */
public final class CorsConfig {

    private static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS";
    private static final String ALLOWED_HEADERS = "Authorization, Content-Type";
    private static final String MAX_AGE         = "3600";

    private static final Set<String> ORIGENS = resolverOrigens();

    private static Set<String> resolverOrigens() {
        Set<String> origens = new LinkedHashSet<>();
        String env = System.getenv("CORS_ORIGIN");
        if (env == null || env.isBlank()) {
            origens.add("http://localhost:5173");
        } else {
            for (String o : env.split(",")) {
                String t = o.trim();
                if (!t.isEmpty()) origens.add(t);
            }
        }
        return origens;
    }

    /**
     * Aplica os headers CORS quando a {@code Origin} da requisição é permitida.
     * Requisições sem header {@code Origin} (same-origin / não-browser) passam direto.
     */
    public static void aplicar(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        if (origin != null && ORIGENS.contains(origin)) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
            resp.addHeader("Vary", "Origin");
        }
    }

    /** Headers adicionais da resposta de preflight ({@code OPTIONS}). */
    public static void aplicarPreflight(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Methods", ALLOWED_METHODS);
        resp.setHeader("Access-Control-Allow-Headers", ALLOWED_HEADERS);
        resp.setHeader("Access-Control-Max-Age", MAX_AGE);
    }

    public static boolean isPreflight(HttpServletRequest req) {
        return "OPTIONS".equalsIgnoreCase(req.getMethod());
    }

    private CorsConfig() {}
}
