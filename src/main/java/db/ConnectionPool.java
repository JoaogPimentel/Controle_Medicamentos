package db;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import utils.DotEnv;

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
            // Credenciais vêm do arquivo .env (raiz do projeto); uma variável de
            // ambiente do processo, se definida, tem precedência sobre o .env.
            String url      = DotEnv.get("DB_URL");
            String user     = DotEnv.get("DB_USER");
            String password = DotEnv.get("DB_PASSWORD");

            if (url == null || user == null || password == null) {
                throw new RuntimeException("Configuração do banco ausente: defina "
                        + "DB_URL, DB_USER e DB_PASSWORD no arquivo .env (raiz do projeto) "
                        + "ou nas variáveis de ambiente.");
            }

            instancia = new ConnectionPool(url, user, password);
        }
        return instancia;
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
