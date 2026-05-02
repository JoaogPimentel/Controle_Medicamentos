package servlet;

import dao.MedicamentoDAO;
import dao.MovimentacaoEstoqueDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Medicamento;
import model.MovimentacaoEstoque;
import model.TipoMovimentacao;
import utils.JsonUtil;

import java.io.IOException;
import java.util.List;

public class EstoqueServlet extends HttpServlet {

    private final MovimentacaoEstoqueDAO movimentacaoDAO = new MovimentacaoEstoqueDAO();
    private final MedicamentoDAO medicamentoDAO = new MedicamentoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String idParam = req.getParameter("medicamento");
            if (idParam == null) {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("Parâmetro 'medicamento' obrigatório."));
                return;
            }
            int idMedicamento = Integer.parseInt(idParam);
            List<MovimentacaoEstoque> lista = movimentacaoDAO.findByMedicamento(idMedicamento);

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                if (i > 0) json.append(",");
                json.append(movimentacaoToJson(lista.get(i)));
            }
            json.append("]");
            JsonUtil.send(resp, HttpServletResponse.SC_OK, json.toString());
        } catch (NumberFormatException e) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("ID inválido."));
        } catch (Exception e) {
            JsonUtil.send(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonUtil.error("Erro interno no servidor."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String body = JsonUtil.readBody(req);
            Integer idMedicamento = JsonUtil.getInt(body, "id_medicamento");
            Integer idResponsavel = JsonUtil.getInt(body, "id_responsavel");
            Double quantidade = JsonUtil.getDouble(body, "quantidade");
            String tipoStr = JsonUtil.getString(body, "tipo");
            String observacao = JsonUtil.getString(body, "observacao");

            if (idMedicamento == null || idResponsavel == null || quantidade == null || tipoStr == null) {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST,
                        JsonUtil.error("Campos obrigatórios: id_medicamento, id_responsavel, quantidade, tipo."));
                return;
            }

            TipoMovimentacao tipo;
            try {
                tipo = TipoMovimentacao.valueOf(tipoStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("Tipo de movimentação inválido."));
                return;
            }

            if (tipo != TipoMovimentacao.ENTRADA_COMPRA && tipo != TipoMovimentacao.ENTRADA_AJUSTE) {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST,
                        JsonUtil.error("Apenas entradas são aceitas: ENTRADA_COMPRA ou ENTRADA_AJUSTE."));
                return;
            }

            Medicamento medicamento = medicamentoDAO.findById(idMedicamento);
            if (medicamento == null) {
                JsonUtil.send(resp, HttpServletResponse.SC_NOT_FOUND, JsonUtil.error("Medicamento não encontrado."));
                return;
            }

            double estoqueAntes = medicamento.getEstoque_atual();
            double estoqueDepois = estoqueAntes + quantidade;

            MovimentacaoEstoque mov = new MovimentacaoEstoque();
            mov.setId_medicamento(idMedicamento);
            mov.setId_registrado_por(idResponsavel);
            mov.setTipo(tipo);
            mov.setQuantidade(quantidade);
            mov.setEstoque_antes(estoqueAntes);
            mov.setEstoque_depois(estoqueDepois);
            mov.setObservacao(observacao);
            movimentacaoDAO.insert(mov);

            JsonUtil.send(resp, HttpServletResponse.SC_CREATED, movimentacaoToJson(mov));
        } catch (Exception e) {
            JsonUtil.send(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonUtil.error("Erro interno no servidor."));
        }
    }

    private String movimentacaoToJson(MovimentacaoEstoque m) {
        return "{"
            + "\"id_movimentacao\":"    + m.getId_movimentacao()    + ","
            + "\"id_medicamento\":"     + m.getId_medicamento()     + ","
            + "\"id_registrado_por\":"  + m.getId_registrado_por()  + ","
            + "\"tipo\":\""             + m.getTipo()               + "\","
            + "\"quantidade\":"         + m.getQuantidade()         + ","
            + "\"estoque_antes\":"      + m.getEstoque_antes()      + ","
            + "\"estoque_depois\":"     + m.getEstoque_depois()     + ","
            + "\"observacao\":"         + (m.getObservacao() != null ? "\"" + JsonUtil.escape(m.getObservacao()) + "\"" : "null") + ","
            + "\"data_movimentacao\":\"" + m.getData_movimentacao() + "\""
            + "}";
    }
}
