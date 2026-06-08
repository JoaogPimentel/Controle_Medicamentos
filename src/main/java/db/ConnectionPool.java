package db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConnectionPool {

    private static final int TAMANHO_POOL = 10;
    private static final long TIMEOUT_SEGUNDOS = 30;

    private static ConnectionPool instancia;

    private final BlockingQueue<Connection> pool;
    private final String url;
    private final String user;
    private final String password;

    private ConnectionPool(String url, String user, String password) throws SQLException {
        this.url      = url;
        this.user     = user;
        this.password = password;
        this.pool     = new ArrayBlockingQueue<>(TAMANHO_POOL);
        for (int i = 0; i < TAMANHO_POOL; i++) {
            pool.offer(criarConexaoReal());
        }
    }

    public static synchronized ConnectionPool getInstance() throws SQLException {
        if (instancia == null) {
            // Carrega db.properties se existir; caso contrário, segue só com env vars.
            Properties props = new Properties();
            File arquivo = new File("src/main/resources/db.properties");
            if (arquivo.exists()) {
                try (InputStream in = new FileInputStream(arquivo)) {
                    props.load(in);
                } catch (IOException e) {
                    throw new RuntimeException("Erro ao carregar db.properties", e);
                }
            }

            // Variáveis de ambiente (ex.: docker-compose) têm precedência sobre o arquivo.
            String url      = resolver("DB_URL",      props, "db.url");
            String user     = resolver("DB_USER",     props, "db.user");
            String password = resolver("DB_PASSWORD", props, "db.password");

            if (url == null || user == null || password == null) {
                throw new RuntimeException("Configuração do banco ausente: defina "
                        + "src/main/resources/db.properties ou as variáveis de ambiente "
                        + "DB_URL, DB_USER e DB_PASSWORD.");
            }

            instancia = new ConnectionPool(url, user, password);
        }
        return instancia;
    }

    /** Resolve um valor priorizando a variável de ambiente sobre o db.properties. */
    private static String resolver(String envVar, Properties props, String chaveProps) {
        String env = System.getenv(envVar);
        if (env != null && !env.trim().isEmpty()) {
            return env.trim();
        }
        String valor = props.getProperty(chaveProps);
        return valor != null ? valor.trim() : null;
    }

    public Connection getConnection() throws SQLException {
        try {
            Connection conn = pool.poll(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
            if (conn == null) {
                throw new SQLException("Timeout: nenhuma conexão disponível no pool após "
                        + TIMEOUT_SEGUNDOS + "s.");
            }
            if (conn.isClosed() || !conn.isValid(2)) {
                conn = criarConexaoReal();
            }
            return embrulharConexao(conn);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Thread interrompida aguardando conexão do pool.", e);
        }
    }

    private Connection criarConexaoReal() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private Connection embrulharConexao(Connection real) {
        return (Connection) Proxy.newProxyInstance(
            Connection.class.getClassLoader(),
            new Class[]{ Connection.class },
            (proxy, method, args) -> {
                if ("close".equals(method.getName())) {
                    try {
                        if (!real.isClosed()) pool.offer(real);
                    } catch (Exception ignored) {}
                    return null;
                }
                return method.invoke(real, args);
            }
        );
    }
}
