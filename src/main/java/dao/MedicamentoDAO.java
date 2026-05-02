package dao;

import db.ConexaoDB;
import model.Medicamento;
import model.StatusMedicamento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicamentoDAO {

    public void insert(Medicamento medicamento) throws SQLException {
        String sql = "INSERT INTO medicamento (id_paciente, id_catalogo, id_cadastrado_por, dosagem, estoque_minimo, data_validade, status) VALUES (?, ?, ?, ?, ?, ?, ?::status_medicamento_enum)";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, medicamento.getId_paciente());
            stmt.setInt(2, medicamento.getId_catalogo());
            stmt.setInt(3, medicamento.getId_cadastrado_por());
            stmt.setString(4, medicamento.getDosagem());
            stmt.setDouble(5, medicamento.getEstoque_minimo());
            stmt.setDate(6, medicamento.getData_validade());
            stmt.setString(7, medicamento.getStatus().name());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) medicamento.setId_medicamento(rs.getInt(1));
            }
        }
    }

    // estoque_atual não é atualizado aqui — é gerenciado pela trigger via MovimentacaoEstoque
    public void update(Medicamento medicamento) throws SQLException {
        String sql = "UPDATE medicamento SET dosagem = ?, estoque_minimo = ?, data_validade = ?, status = ?::status_medicamento_enum WHERE id_medicamento = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, medicamento.getDosagem());
            stmt.setDouble(2, medicamento.getEstoque_minimo());
            stmt.setDate(3, medicamento.getData_validade());
            stmt.setString(4, medicamento.getStatus().name());
            stmt.setInt(5, medicamento.getId_medicamento());
            stmt.executeUpdate();
        }
    }

    public Medicamento findById(int id_medicamento) throws SQLException {
        String sql = "SELECT * FROM medicamento WHERE id_medicamento = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_medicamento);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public List<Medicamento> findByPaciente(int id_paciente) throws SQLException {
        String sql = "SELECT * FROM medicamento WHERE id_paciente = ?";
        List<Medicamento> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_paciente);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Medicamento> findEmUsoByPaciente(int id_paciente) throws SQLException {
        String sql = "SELECT * FROM medicamento WHERE id_paciente = ? AND status = 'EM_USO'";
        List<Medicamento> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_paciente);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Medicamento mapear(ResultSet rs) throws SQLException {
        Medicamento m = new Medicamento();
        m.setId_medicamento(rs.getInt("id_medicamento"));
        m.setId_paciente(rs.getInt("id_paciente"));
        m.setId_catalogo(rs.getInt("id_catalogo"));
        m.setId_cadastrado_por(rs.getInt("id_cadastrado_por"));
        m.setDosagem(rs.getString("dosagem"));
        m.setEstoque_atual(rs.getDouble("estoque_atual"));
        m.setEstoque_minimo(rs.getDouble("estoque_minimo"));
        m.setData_validade(rs.getDate("data_validade"));
        m.setStatus(StatusMedicamento.valueOf(rs.getString("status")));
        m.setData_primeiro_uso(rs.getDate("data_primeiro_uso"));
        m.setData_cadastro(rs.getDate("data_cadastro"));
        m.setData_atualizacao(rs.getDate("data_atualizacao"));
        return m;
    }
}
