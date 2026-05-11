package dao;

import db.ConexaoDB;
import model.RolePessoa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RoleDAO {

    public RolePessoa findRole(int idPessoa) throws SQLException {
        if (existeCuidador(idPessoa)) return RolePessoa.CUIDADOR;
        if (existePaciente(idPessoa)) return RolePessoa.PACIENTE;
        return RolePessoa.PACIENTE;
    }

    private boolean existeCuidador(int idPessoa) throws SQLException {
        String sql = "SELECT 1 FROM cuidador WHERE id_pessoa = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPessoa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean existePaciente(int idPessoa) throws SQLException {
        String sql = "SELECT 1 FROM paciente WHERE id_pessoa = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPessoa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
