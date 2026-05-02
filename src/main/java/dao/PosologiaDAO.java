package dao;

import db.ConexaoDB;
import model.Posologia;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PosologiaDAO {

    public void insert(Posologia posologia) throws SQLException {
        String sql = "INSERT INTO posologia (id_medicamento, horario_primeira_dose, intervalo_horas, quantidade_por_dose, duracao_dias, data_inicio, ativo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, posologia.getId_medicamento());
            stmt.setTime(2, posologia.getHorario_primeira_dose());
            stmt.setInt(3, posologia.getIntervalo_horas());
            stmt.setDouble(4, posologia.getQuantidade_por_dose());
            stmt.setObject(5, posologia.getDuracao_dias(), Types.INTEGER);
            stmt.setDate(6, new java.sql.Date(posologia.getData_inicio().getTime()));
            stmt.setBoolean(7, posologia.getAtivo());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) posologia.setId_posologia(rs.getInt(1));
            }
        }
    }

    public void desativar(int id_posologia) throws SQLException {
        String sql = "UPDATE posologia SET ativo = FALSE WHERE id_posologia = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_posologia);
            stmt.executeUpdate();
        }
    }

    public Posologia findById(int id_posologia) throws SQLException {
        String sql = "SELECT * FROM posologia WHERE id_posologia = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_posologia);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public List<Posologia> findByMedicamento(int id_medicamento) throws SQLException {
        String sql = "SELECT * FROM posologia WHERE id_medicamento = ? ORDER BY data_inicio DESC";
        List<Posologia> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_medicamento);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Posologia> findAtivasByMedicamento(int id_medicamento) throws SQLException {
        String sql = "SELECT * FROM posologia WHERE id_medicamento = ? AND ativo = TRUE";
        List<Posologia> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_medicamento);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Posologia mapear(ResultSet rs) throws SQLException {
        Posologia p = new Posologia();
        p.setId_posologia(rs.getInt("id_posologia"));
        p.setId_medicamento(rs.getInt("id_medicamento"));
        p.setHorario_primeira_dose(rs.getTime("horario_primeira_dose"));
        p.setIntervalo_horas(rs.getInt("intervalo_horas"));
        p.setQuantidade_por_dose(rs.getDouble("quantidade_por_dose"));
        int duracao = rs.getInt("duracao_dias");
        p.setDuracao_dias(rs.wasNull() ? null : duracao);
        p.setData_inicio(rs.getDate("data_inicio"));
        p.setAtivo(rs.getBoolean("ativo"));
        p.setData_cadastro(rs.getTimestamp("data_cadastro"));
        p.setData_atualizacao(rs.getTimestamp("data_atualizacao"));
        return p;
    }
}
