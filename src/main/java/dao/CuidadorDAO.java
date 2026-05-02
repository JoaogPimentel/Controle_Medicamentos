package dao;

import db.ConexaoDB;
import model.Cuidador;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CuidadorDAO {

    public void insert(Cuidador cuidador) throws SQLException {
        String sql = "INSERT INTO cuidador (id_pessoa, profissional_saude, registro_profissional) VALUES (?, ?, ?)";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cuidador.getId_pessoa());
            stmt.setBoolean(2, cuidador.isProfissional_saude());
            stmt.setString(3, cuidador.getRegistro_profissional());
            stmt.executeUpdate();
        }
    }

    public void update(Cuidador cuidador) throws SQLException {
        String sql = "UPDATE cuidador SET profissional_saude = ?, registro_profissional = ? WHERE id_pessoa = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, cuidador.isProfissional_saude());
            stmt.setString(2, cuidador.getRegistro_profissional());
            stmt.setInt(3, cuidador.getId_pessoa());
            stmt.executeUpdate();
        }
    }

    public Cuidador findById(int id_pessoa) throws SQLException {
        String sql = "SELECT * FROM cuidador WHERE id_pessoa = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_pessoa);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public List<Cuidador> findAll() throws SQLException {
        String sql = "SELECT * FROM cuidador";
        List<Cuidador> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private Cuidador mapear(ResultSet rs) throws SQLException {
        Cuidador c = new Cuidador();
        c.setId_pessoa(rs.getInt("id_pessoa"));
        c.setProfissional_saude(rs.getBoolean("profissional_saude"));
        c.setRegistro_profissional(rs.getString("registro_profissional"));
        c.setData_virou_cuidador(rs.getDate("data_virou_cuidador"));
        c.setData_atualizacao(rs.getDate("data_atualizacao"));
        return c;
    }
}
