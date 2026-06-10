package utils;

import model.RolePessoa;
import model.UsuarioSessao;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Geração e validação de JSON Web Tokens (JWT) com assinatura HS256.
 *
 * Implementação sem dependências externas: usa apenas {@code javax.crypto}
 * (HMAC-SHA256) e {@link Base64} url-safe. O segredo de assinatura vem da
 * variável de ambiente {@code JWT_SECRET}.
 *
 * Claims do payload: {@code id_pessoa}, {@code nome}, {@code papel},
 * {@code iat} (emissão) e {@code exp} (expiração) — ambos em epoch seconds.
 */
public class JwtUtil {

    /** Validade do token em segundos (8 horas). */
    private static final long EXPIRACAO_SEGUNDOS = 8 * 60 * 60;

    private static final String SECRET = resolverSecret();

    private static final Base64.Encoder ENC = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DEC = Base64.getUrlDecoder();

    private static String resolverSecret() {
        String s = DotEnv.get("JWT_SECRET");
        if (s == null || s.isBlank()) {
            System.err.println("[AVISO] JWT_SECRET não definido — usando segredo de "
                + "desenvolvimento. NÃO use este valor em produção.");
            return "dev-secret-troque-em-producao";
        }
        return s;
    }

    /** Gera um token assinado para o usuário informado. */
    public static String gerar(int idPessoa, String nome, RolePessoa papel) {
        long agora = System.currentTimeMillis() / 1000L;
        long exp   = agora + EXPIRACAO_SEGUNDOS;

        String header = ENC.encodeToString(
            "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

        String payload = ENC.encodeToString((
            "{\"id_pessoa\":" + idPessoa
            + ",\"nome\":\""  + JsonUtil.escape(nome) + "\""
            + ",\"papel\":\"" + papel.name() + "\""
            + ",\"iat\":"     + agora
            + ",\"exp\":"     + exp
            + "}").getBytes(StandardCharsets.UTF_8));

        String dados = header + "." + payload;
        return dados + "." + assinar(dados);
    }

    /**
     * Valida a assinatura e a expiração do token e devolve o usuário
     * correspondente, ou {@code null} se o token for inválido/expirado.
     */
    public static UsuarioSessao validar(String token) {
        if (token == null) return null;

        String[] partes = token.split("\\.");
        if (partes.length != 3) return null;

        String esperada = assinar(partes[0] + "." + partes[1]);
        if (!constantTimeEquals(esperada, partes[2])) return null;

        String payload;
        try {
            payload = new String(DEC.decode(partes[1]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }

        long exp = lerLong(payload, "exp");
        if (exp > 0 && (System.currentTimeMillis() / 1000L) >= exp) return null;

        Integer id      = JsonUtil.getInt(payload, "id_pessoa");
        String nome     = JsonUtil.getString(payload, "nome");
        String papelStr = JsonUtil.getString(payload, "papel");
        if (id == null || papelStr == null) return null;

        RolePessoa papel;
        try {
            papel = RolePessoa.valueOf(papelStr);
        } catch (IllegalArgumentException e) {
            return null;
        }

        // O e-mail não é um claim do token (não é necessário no fluxo de API).
        return new UsuarioSessao(id, nome, null, papel);
    }

    private static String assinar(String dados) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return ENC.encodeToString(mac.doFinal(dados.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao assinar o JWT", e);
        }
    }

    /** Lê um valor numérico inteiro (long) de um JSON simples; -1 se ausente. */
    private static long lerLong(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return -1;
        idx = json.indexOf(":", idx) + 1;
        while (idx < json.length() && Character.isWhitespace(json.charAt(idx))) idx++;
        int end = idx;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        if (end == idx) return -1;
        return Long.parseLong(json.substring(idx, end));
    }

    /** Comparação em tempo constante para evitar timing attacks na assinatura. */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] x = a.getBytes(StandardCharsets.UTF_8);
        byte[] y = b.getBytes(StandardCharsets.UTF_8);
        if (x.length != y.length) return false;
        int r = 0;
        for (int i = 0; i < x.length; i++) r |= x[i] ^ y[i];
        return r == 0;
    }

    private JwtUtil() {}
}
