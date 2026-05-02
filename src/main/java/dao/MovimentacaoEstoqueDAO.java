package dao;

import db.ConexaoDB;
import model.MovimentacaoEstoque;
import model.TipoMovimentacao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovimentacaoEstoqueDAO {

    // Sem update/delete — movimentação é um log imutável de auditoria
    public void insert(MovimentacaoEstoque mov) throws SQLException {
        String sql = "INSERT INTO movimentacao_estoque (id_medicamento, id_registrado_por, tipo, quantidade, estoque_antes, estoque_depois, id_historico_uso, observacao) VALUES (?, ?, ?::tipo_movimentacao_enum, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, mov.getId_medicamento());
            stmt.setInt(2, mov.getId_registrado_por());
            stmt.setString(3, mov.getTipo().name());
            stmt.setDouble(4, mov.getQuantidade());
            stmt.setDouble(5, mov.getEstoque_antes());
            stmt.setDouble(6, mov.getEstoque_depois());
            stmt.setObject(7, mov.getId_historico_uso(), Types.INTEGER);
            stmt.setString(8, mov.getObservacao());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) mov.setId_movimentacao(rs.getInt(1));
            }
        }
    }

    public MovimentacaoEstoque findById(int id_movimentacao) throws SQLException {
        String sql = "SELECT * FROM movimentacao_estoque WHERE id_movimentacao = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_movimentacao);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public List<MovimentacaoEstoque> findByMedicamento(int id_medicamento) throws SQLException {
        String sql = "SELECT * FROM movimentacao_estoque WHERE id_medicamento = ? ORDER BY data_movimentacao DESC";
        List<MovimentacaoEstoque> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id_medicamento);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private MovimentacaoEstoque mapear(ResultSet rs) throws SQLException {
        MovimentacaoEstoque m = new MovimentacaoEstoque();
        m.setId_movimentacao(rs.getInt("id_movimentacao"));
        m.setId_medicamento(rs.getInt("id_medicamento"));
        m.setId_registrado_por(rs.getInt("id_registrado_por"));
        m.setTipo(TipoMovimentacao.valueOf(rs.getString("tipo")));
        m.setQuantidade(rs.getDouble("quantidade"));
        m.setEstoque_antes(rs.getDouble("estoque_antes"));
        m.setEstoque_depois(rs.getDouble("estoque_depois"));
        int idHist = rs.getInt("id_historico_uso");
        m.setId_historico_uso(rs.wasNull() ? null : idHist);
        m.setObservacao(rs.getString("observacao"));
        m.setData_movimentacao(rs.getTimestamp("data_movimentacao"));
        return m;
    }
}
