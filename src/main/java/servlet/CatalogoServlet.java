package servlet;

import dao.MedicamentoCatalogoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.FormaFarmaceutica;
import model.MedicamentoCatalogo;
import utils.JsonUtil;

import java.io.IOException;
import java.util.List;

public class CatalogoServlet extends HttpServlet {

    private final MedicamentoCatalogoDAO dao = new MedicamentoCatalogoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String path = req.getPathInfo();
            if (path != null && !path.equals("/")) {
                int id = Integer.parseInt(path.substring(1));
                MedicamentoCatalogo c = dao.findById(id);
                if (c == null) {
                    JsonUtil.send(resp, HttpServletResponse.SC_NOT_FOUND, JsonUtil.error("Catálogo não encontrado."));
                    return;
                }
                JsonUtil.send(resp, HttpServletResponse.SC_OK, catalogoToJson(c));
                return;
            }

            String nome = req.getParameter("nome");
            List<MedicamentoCatalogo> lista = (nome != null && !nome.isBlank())
                    ? dao.findByNome(nome)
                    : dao.findAll();

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                if (i > 0) json.append(",");
                json.append(catalogoToJson(lista.get(i)));
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
            String nome = JsonUtil.getString(body, "nome");
            String principioAtivo = JsonUtil.getString(body, "principio_ativo");
            String forma = JsonUtil.getString(body, "forma_farmaceutica");

            if (nome == null || principioAtivo == null || forma == null) {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST,
                        JsonUtil.error("Campos obrigatórios: nome, principio_ativo, forma_farmaceutica."));
                return;
            }

            FormaFarmaceutica ff;
            try {
                ff = FormaFarmaceutica.valueOf(forma.toUpperCase());
            } catch (IllegalArgumentException e) {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("forma_farmaceutica inválida."));
                return;
            }

            MedicamentoCatalogo c = new MedicamentoCatalogo(null, nome, principioAtivo, null, null, ff);
            dao.insert(c);
            JsonUtil.send(resp, HttpServletResponse.SC_CREATED, catalogoToJson(c));
        } catch (Exception e) {
            JsonUtil.send(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonUtil.error("Erro interno no servidor."));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String path = req.getPathInfo();
            if (path == null || path.equals("/")) {
                JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("ID do catálogo não informado."));
                return;
            }
            int id = Integer.parseInt(path.substring(1));
            MedicamentoCatalogo c = dao.findById(id);
            if (c == null) {
                JsonUtil.send(resp, HttpServletResponse.SC_NOT_FOUND, JsonUtil.error("Catálogo não encontrado."));
                return;
            }

            String body = JsonUtil.readBody(req);
            String nome = JsonUtil.getString(body, "nome");
            String principioAtivo = JsonUtil.getString(body, "principio_ativo");
            String forma = JsonUtil.getString(body, "forma_farmaceutica");

            if (nome != null) c.setNome(nome);
            if (principioAtivo != null) c.setPrincipio_ativo(principioAtivo);
            if (forma != null) {
                try {
                    c.setForma_farmaceutica(FormaFarmaceutica.valueOf(forma.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("forma_farmaceutica inválida."));
                    return;
                }
            }

            dao.update(c);
            JsonUtil.send(resp, HttpServletResponse.SC_OK, catalogoToJson(c));
        } catch (NumberFormatException e) {
            JsonUtil.send(resp, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("ID inválido."));
        } catch (Exception e) {
            JsonUtil.send(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonUtil.error("Erro interno no servidor."));
        }
    }

    private String catalogoToJson(MedicamentoCatalogo c) {
        return "{"
            + "\"id_catalogo\":"          + c.getId_catalogo() + ","
            + "\"nome\":\""               + JsonUtil.escape(c.getNome()) + "\","
            + "\"principio_ativo\":\""    + JsonUtil.escape(c.getPrincipio_ativo()) + "\","
            + "\"forma_farmaceutica\":\"" + c.getForma_farmaceutica() + "\","
            + "\"data_cadastro\":"        + (c.getData_cadastro() != null ? "\"" + c.getData_cadastro() + "\"" : "null")
            + "}";
    }
}
