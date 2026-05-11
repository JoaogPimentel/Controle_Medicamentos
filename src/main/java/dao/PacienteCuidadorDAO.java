package dao;

import db.ConexaoDB;
import model.PacienteCuidador;
import model.Pessoa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacienteCuidadorDAO {

    public void insert(PacienteCuidador vinculo) throws SQLException {
        String sql = "INSERT INTO paciente_cuidador (id_paciente, id_cuidador, ativo) VALUES (?, ?, ?)";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, vinculo.getId_paciente());
            stmt.setInt(2, vinculo.getId_cuidador());
            stmt.setBoolean(3, vinculo.isAtivo());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) vinculo.setId_vinculo(rs.getInt(1));
            }
        }
    }

    public void encerrarVinculo(int id_vinculo) throws SQLException {
        String sql = "UPDATE paciente_cuidador SET ativo = FALSE, data_fim = CURRENT_TIMESTAMP WHERE id_vinculo = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_vinculo);
            stmt.executeUpdate();
        }
    }

    public PacienteCuidador findById(int id_vinculo) throws SQLException {
        String sql = "SELECT * FROM paciente_cuidador WHERE id_vinculo = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_vinculo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public List<PacienteCuidador> findAtivosByPaciente(int id_paciente) throws SQLException {
        String sql = "SELECT * FROM paciente_cuidador WHERE id_paciente = ? AND ativo = TRUE";
        List<PacienteCuidador> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_paciente);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<PacienteCuidador> findAtivosByCuidador(int id_cuidador) throws SQLException {
        String sql = "SELECT * FROM paciente_cuidador WHERE id_cuidador = ? AND ativo = TRUE";
        List<PacienteCuidador> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_cuidador);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Pessoa> findPacientesDoCuidador(int idCuidador) throws SQLException {
        String sql = "SELECT p.id_pessoa, p.nome, p.email, p.telefone, p.ativo " +
                     "FROM pessoa p " +
                     "INNER JOIN paciente_cuidador pc ON p.id_pessoa = pc.id_paciente " +
                     "WHERE pc.id_cuidador = ? AND pc.ativo = TRUE " +
                     "ORDER BY p.nome";
        List<Pessoa> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCuidador);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Pessoa p = new Pessoa();
                    p.setId_pessoa(rs.getInt("id_pessoa"));
                    p.setNome(rs.getString("nome"));
                    p.setEmail(rs.getString("email"));
                    p.setTelefone(rs.getString("telefone"));
                    p.setAtivo(rs.getBoolean("ativo"));
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    private PacienteCuidador mapear(ResultSet rs) throws SQLException {
        PacienteCuidador pc = new PacienteCuidador();
        pc.setId_vinculo(rs.getInt("id_vinculo"));
        pc.setId_paciente(rs.getInt("id_paciente"));
        pc.setId_cuidador(rs.getInt("id_cuidador"));
        pc.setData_vinculo(rs.getDate("data_vinculo"));
        pc.setData_fim(rs.getDate("data_fim"));
        pc.setAtivo(rs.getBoolean("ativo"));
        pc.setData_atualizacao(rs.getDate("data_atualizacao"));
        return pc;
    }
}
