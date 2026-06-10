package utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Leitor simples do arquivo {@code .env} (na raiz do projeto), usado como fonte
 * de configuração local — credenciais do banco, segredo do JWT, origens de CORS.
 *
 * As variáveis de ambiente do processo têm precedência sobre o {@code .env}:
 * assim, em produção/Docker o ambiente real sobrepõe o arquivo sem que ele
 * precise existir.
 *
 * Formato: linhas {@code CHAVE=VALOR}. Linhas em branco e iniciadas por
 * {@code #} são ignoradas. Um {@code #} no meio do valor (ex.: numa senha)
 * NÃO inicia comentário. Aspas externas, se houver, são removidas.
 */
public final class DotEnv {

    private static final Map<String, String> VARS = carregar();

    private static Map<String, String> carregar() {
        Map<String, String> vars = new HashMap<>();
        File arquivo = new File(".env");
        if (!arquivo.exists()) {
            return vars;
        }
        try {
            for (String linha : Files.readAllLines(arquivo.toPath(), StandardCharsets.UTF_8)) {
                String t = linha.trim();
                if (t.isEmpty() || t.startsWith("#")) continue;
                int eq = t.indexOf('=');
                if (eq <= 0) continue;
                String chave = t.substring(0, eq).trim();
                String valor = t.substring(eq + 1).trim();
                if (valor.length() >= 2
                        && ((valor.startsWith("\"") && valor.endsWith("\""))
                         || (valor.startsWith("'")  && valor.endsWith("'")))) {
                    valor = valor.substring(1, valor.length() - 1);
                }
                vars.put(chave, valor);
            }
        } catch (IOException e) {
            System.err.println("[AVISO] Falha ao ler o arquivo .env: " + e.getMessage());
        }
        return vars;
    }

    /** Valor da chave; a variável de ambiente do processo tem precedência sobre o {@code .env}. */
    public static String get(String chave) {
        String env = System.getenv(chave);
        if (env != null && !env.trim().isEmpty()) {
            return env.trim();
        }
        String valor = VARS.get(chave);
        return (valor != null && !valor.trim().isEmpty()) ? valor.trim() : null;
    }

    /** Valor da chave ou {@code padrao} quando ausente. */
    public static String get(String chave, String padrao) {
        String v = get(chave);
        return v != null ? v : padrao;
    }

    private DotEnv() {}
}
