package dao;

import db.ConexaoDB;
import model.FormaFarmaceutica;
import model.MedicamentoCatalogo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicamentoCatalogoDAO {

    public void insert(MedicamentoCatalogo catalogo) throws SQLException {
        String sql = "INSERT INTO medicamento_catalogo (nome, principio_ativo, forma_farmaceutica) VALUES (?, ?, ?::forma_farmaceutica_enum)";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, catalogo.getNome());
            stmt.setString(2, catalogo.getPrincipio_ativo());
            stmt.setString(3, catalogo.getForma_farmaceutica().name());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) catalogo.setId_catalogo(rs.getInt(1));
            }
        }
    }

    public boolean temMedicamentosVinculados(int id) throws SQLException {
        String sql = "SELECT 1 FROM medicamento WHERE id_catalogo = ? LIMIT 1";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM medicamento_catalogo WHERE id_catalogo = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void update(MedicamentoCatalogo catalogo) throws SQLException {
        String sql = "UPDATE medicamento_catalogo SET nome = ?, principio_ativo = ?, forma_farmaceutica = ?::forma_farmaceutica_enum WHERE id_catalogo = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, catalogo.getNome());
            stmt.setString(2, catalogo.getPrincipio_ativo());
            stmt.setString(3, catalogo.getForma_farmaceutica().name());
            stmt.setInt(4, catalogo.getId_catalogo());
            stmt.executeUpdate();
        }
    }

    public MedicamentoCatalogo findById(int id_catalogo) throws SQLException {
        String sql = "SELECT * FROM medicamento_catalogo WHERE id_catalogo = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_catalogo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public List<MedicamentoCatalogo> findByNome(String nome) throws SQLException {
        String sql = "SELECT * FROM medicamento_catalogo WHERE nome ILIKE ?";
        List<MedicamentoCatalogo> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + nome + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<MedicamentoCatalogo> findAll() throws SQLException {
        String sql = "SELECT * FROM medicamento_catalogo ORDER BY nome";
        List<MedicamentoCatalogo> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private MedicamentoCatalogo mapear(ResultSet rs) throws SQLException {
        return new MedicamentoCatalogo(
            rs.getInt("id_catalogo"),
            rs.getString("nome"),
            rs.getString("principio_ativo"),
            rs.getDate("data_cadastro"),
            rs.getDate("data_atualizacao"),
            FormaFarmaceutica.valueOf(rs.getString("forma_farmaceutica"))
        );
    }
}
