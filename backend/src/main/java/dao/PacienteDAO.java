package dao;

import db.ConexaoDB;
import model.Paciente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {

    public void insert(Paciente paciente) throws SQLException {
        String sql = "INSERT INTO paciente (id_pessoa, convenio, observacoes_medicas) VALUES (?, ?, ?)";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, paciente.getId_pessoa());
            stmt.setString(2, paciente.getConvenio());
            stmt.setString(3, paciente.getObservacoes_medicas());
            stmt.executeUpdate();
        }
    }

    public void update(Paciente paciente) throws SQLException {
        String sql = "UPDATE paciente SET convenio = ?, observacoes_medicas = ? WHERE id_pessoa = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, paciente.getConvenio());
            stmt.setString(2, paciente.getObservacoes_medicas());
            stmt.setInt(3, paciente.getId_pessoa());
            stmt.executeUpdate();
        }
    }

    public Paciente findById(int id_pessoa) throws SQLException {
        String sql = "SELECT * FROM paciente WHERE id_pessoa = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_pessoa);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public List<Paciente> findAll() throws SQLException {
        String sql = "SELECT * FROM paciente";
        List<Paciente> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private Paciente mapear(ResultSet rs) throws SQLException {
        Paciente p = new Paciente();
        p.setId_pessoa(rs.getInt("id_pessoa"));
        p.setConvenio(rs.getString("convenio"));
        p.setObservacoes_medicas(rs.getString("observacoes_medicas"));
        p.setData_virou_paciente(rs.getDate("data_virou_paciente"));
        p.setData_atualizacao(rs.getDate("data_atualizacao"));
        return p;
    }
}
