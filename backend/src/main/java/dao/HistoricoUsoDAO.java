package dao;

import db.ConexaoDB;
import model.HistoricoUso;
import model.StatusDose;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoricoUsoDAO {

    public void insert(HistoricoUso historico) throws SQLException {
        String sql = "INSERT INTO historico_uso (id_posologia, data_hora, status, id_registrado_por, observacao) VALUES (?, ?, ?::status_dose_enum, ?, ?)";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, historico.getId_posologia());
            stmt.setTimestamp(2, new Timestamp(historico.getData_hora().getTime()));
            stmt.setString(3, historico.getStatus().name());
            stmt.setObject(4, historico.getId_registrado_por(), Types.INTEGER);
            stmt.setString(5, historico.getObservacao());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) historico.setId_historico(rs.getInt(1));
            }
        }
    }

    public void update(HistoricoUso historico) throws SQLException {
        String sql = "UPDATE historico_uso SET status = ?::status_dose_enum, id_registrado_por = ?, observacao = ? WHERE id_historico = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, historico.getStatus().name());
            stmt.setObject(2, historico.getId_registrado_por(), Types.INTEGER);
            stmt.setString(3, historico.getObservacao());
            stmt.setInt(4, historico.getId_historico());
            stmt.executeUpdate();
        }
    }

    public HistoricoUso findById(int id_historico) throws SQLException {
        String sql = "SELECT * FROM historico_uso WHERE id_historico = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_historico);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public List<HistoricoUso> findByPosologia(int id_posologia) throws SQLException {
        String sql = "SELECT * FROM historico_uso WHERE id_posologia = ? ORDER BY data_hora DESC";
        List<HistoricoUso> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_posologia);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private HistoricoUso mapear(ResultSet rs) throws SQLException {
        HistoricoUso h = new HistoricoUso();
        h.setId_historico(rs.getInt("id_historico"));
        h.setId_posologia(rs.getInt("id_posologia"));
        h.setData_hora(rs.getTimestamp("data_hora"));
        h.setStatus(StatusDose.valueOf(rs.getString("status")));
        int idReg = rs.getInt("id_registrado_por");
        h.setId_registrado_por(rs.wasNull() ? null : idReg);
        h.setObservacao(rs.getString("observacao"));
        h.setData_registro(rs.getTimestamp("data_registro"));
        return h;
    }
}
