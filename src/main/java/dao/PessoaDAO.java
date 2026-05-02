package dao;
import db.ConexaoDB;
import model.Pessoa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PessoaDAO {

    public void insert(Pessoa pessoa) throws SQLException {
        String sql = "INSERT INTO pessoa (nome, data_nascimento, email, senha_hash, telefone, ativo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, pessoa.getNome());
            stmt.setDate(2, pessoa.getData_nascimento());
            stmt.setString(3, pessoa.getEmail());
            stmt.setString(4, pessoa.getSenha_hash());
            stmt.setString(5, pessoa.getTelefone());
            stmt.setBoolean(6, pessoa.getAtivo());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) pessoa.setId_pessoa(rs.getInt(1));
            }
        }
    }

    public void update(Pessoa pessoa) throws SQLException {
        String sql = "UPDATE pessoa SET nome = ?, data_nascimento = ?, email = ?, senha_hash = ?, telefone = ?, ativo = ? WHERE id_pessoa = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pessoa.getNome());
            stmt.setDate(2, pessoa.getData_nascimento());
            stmt.setString(3, pessoa.getEmail());
            stmt.setString(4, pessoa.getSenha_hash());
            stmt.setString(5, pessoa.getTelefone());
            stmt.setBoolean(6, pessoa.getAtivo());
            stmt.setInt(7, pessoa.getId_pessoa());
            stmt.executeUpdate();
        }
    }

    public Pessoa findById(int id_pessoa) throws SQLException {
        String sql = "SELECT * FROM pessoa WHERE id_pessoa = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_pessoa);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public Pessoa findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM pessoa WHERE email = ? AND ativo = TRUE";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public List<Pessoa> findAll() throws SQLException {
        String sql = "SELECT * FROM pessoa";
        List<Pessoa> pessoas = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) pessoas.add(mapear(rs));
        }
        return pessoas;
    }

    public List<Pessoa> findByNome(String nome) throws SQLException {
        String sql = "SELECT * FROM pessoa WHERE nome ILIKE ?";
        List<Pessoa> pessoas = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + nome + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) pessoas.add(mapear(rs));
            }
        }
        return pessoas;
    }

    public List<Pessoa> findByAtivo(boolean ativo) throws SQLException {
        String sql = "SELECT * FROM pessoa WHERE ativo = ?";
        List<Pessoa> pessoas = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, ativo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) pessoas.add(mapear(rs));
            }
        }
        return pessoas;
    }

    public void setAtivo(int id_pessoa, boolean ativo) throws SQLException {
        String sql = "UPDATE pessoa SET ativo = ? WHERE id_pessoa = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, ativo);
            stmt.setInt(2, id_pessoa);
            stmt.executeUpdate();
        }
    }

    public void setInativo(int id_pessoa) throws SQLException {
        setAtivo(id_pessoa, false);
    }

    public void setAtivo(int id_pessoa) throws SQLException {
        setAtivo(id_pessoa, true);
    }

    private Pessoa mapear(ResultSet rs) throws SQLException {
        Pessoa pessoa = new Pessoa();
        pessoa.setId_pessoa(rs.getInt("id_pessoa"));
        pessoa.setNome(rs.getString("nome"));
        pessoa.setData_nascimento(rs.getDate("data_nascimento"));
        pessoa.setEmail(rs.getString("email"));
        pessoa.setSenha_hash(rs.getString("senha_hash"));
        pessoa.setTelefone(rs.getString("telefone"));
        pessoa.setAtivo(rs.getBoolean("ativo"));
        pessoa.setData_cadastro(rs.getDate("data_cadastro"));
        pessoa.setData_atualizacao(rs.getDate("data_atualizacao"));
        return pessoa;
    }
}
