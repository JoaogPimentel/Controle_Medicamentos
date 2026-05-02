package db;

import java.sql.Connection;
import java.sql.SQLException;

public class ConexaoDB {

    private ConexaoDB() {}

    /**
     * Obtém uma conexão do pool. Chamar conn.close() a devolve ao pool
     * sem encerrar a conexão real com o banco.
     */
    public static Connection getConnection() throws SQLException {
        return ConnectionPool.getInstance().getConnection();
    }
}