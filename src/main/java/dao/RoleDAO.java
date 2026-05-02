package dao;

import db.ConexaoDB;
import model.RolePessoa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RoleDAO {

    /**
     * Determina o papel do usuário consultando as tabelas existentes.
     * Cuidador tem precedência caso a pessoa seja os dois.
     */
    public RolePessoa findRole(int idPessoa) throws SQLException {
        if (existeEm("cuidador", idPessoa)) return RolePessoa.CUIDADOR;
        if (existeEm("paciente", idPessoa)) return RolePessoa.PACIENTE;
        return RolePessoa.PACIENTE; // padrão para novos cadastros
    }

    private boolean existeEm(String tabela, int idPessoa) throws SQLException {
        String sql = "SELECT 1 FROM " + tabela + " WHERE id_pessoa = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPessoa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
