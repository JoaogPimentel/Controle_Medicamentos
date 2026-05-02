package dao;

import db.ConexaoDB;
import model.Alerta;
import model.TipoAlerta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlertaDAO {

    public void insert(Alerta alerta) throws SQLException {
        String sql = "INSERT INTO alerta (id_pessoa, id_medicamento, tipo, mensagem, lido) VALUES (?, ?, ?::tipo_alerta_enum, ?, ?)";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, alerta.getId_pessoa());
            stmt.setObject(2, alerta.getId_medicamento(), Types.INTEGER);
            stmt.setString(3, alerta.getTipo().name());
            stmt.setString(4, alerta.getMensagem());
            stmt.setBoolean(5, alerta.getLido());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) alerta.setId_alerta(rs.getInt(1));
            }
        }
    }

    public void marcarLido(int id_alerta) throws SQLException {
        String sql = "UPDATE alerta SET lido = TRUE, data_leitura = CURRENT_TIMESTAMP WHERE id_alerta = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_alerta);
            stmt.executeUpdate();
        }
    }

    public Alerta findById(int id_alerta) throws SQLException {
        String sql = "SELECT * FROM alerta WHERE id_alerta = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_alerta);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public List<Alerta> findNaoLidosByPessoa(int id_pessoa) throws SQLException {
        String sql = "SELECT * FROM alerta WHERE id_pessoa = ? AND lido = FALSE ORDER BY data_geracao DESC";
        List<Alerta> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_pessoa);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Alerta> findByPessoa(int id_pessoa) throws SQLException {
        String sql = "SELECT * FROM alerta WHERE id_pessoa = ? ORDER BY data_geracao DESC";
        List<Alerta> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_pessoa);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Alerta mapear(ResultSet rs) throws SQLException {
        Alerta a = new Alerta();
        a.setId_alerta(rs.getInt("id_alerta"));
        a.setId_pessoa(rs.getInt("id_pessoa"));
        int idMed = rs.getInt("id_medicamento");
        a.setId_medicamento(rs.wasNull() ? null : idMed);
        a.setTipo(TipoAlerta.valueOf(rs.getString("tipo")));
        a.setMensagem(rs.getString("mensagem"));
        a.setLido(rs.getBoolean("lido"));
        a.setData_geracao(rs.getTimestamp("data_geracao"));
        a.setData_leitura(rs.getTimestamp("data_leitura"));
        return a;
    }
}
