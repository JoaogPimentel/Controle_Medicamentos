package servlet;

import dao.PacienteCuidadorDAO;
import services.AlertaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Alerta;
import model.Pessoa;
import model.RolePessoa;
import model.UsuarioSessao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        UsuarioSessao usuario = (UsuarioSessao)
            req.getSession(false).getAttribute(AuthServlet.ATTR_USUARIO);

        req.setAttribute("usuario", usuario);

        if (usuario.getPapel() == RolePessoa.CUIDADOR) {
            carregarDadosCuidador(req, usuario.getIdPessoa());
        } else {
            carregarDadosPaciente(req, usuario.getIdPessoa());
        }

        req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
    }

    private void carregarDadosPaciente(HttpServletRequest req, int idPessoa) {
        List<Alerta> alertas;
        try {
            AlertaService alertaService = new AlertaService();
            alertaService.verificarAlertas(idPessoa);
            alertas = alertaService.listarNaoLidos(idPessoa);
        } catch (SQLException e) {
            alertas = Collections.emptyList();
        }
        req.setAttribute("alertas", alertas);
    }

    private void carregarDadosCuidador(HttpServletRequest req, int idCuidador) {
        List<Pessoa> pacientes = Collections.emptyList();
        Map<Integer, Integer> alertasPorPaciente = new HashMap<>();
        try {
            pacientes = new PacienteCuidadorDAO().findPacientesDoCuidador(idCuidador);
            AlertaService alertaService = new AlertaService();
            for (Pessoa p : pacientes) {
                alertaService.verificarAlertas(p.getId_pessoa());
                int total = alertaService.listarNaoLidos(p.getId_pessoa()).size();
                alertasPorPaciente.put(p.getId_pessoa(), total);
            }
        } catch (SQLException e) {
        }
        req.setAttribute("pacientes", pacientes);
        req.setAttribute("alertasPorPaciente", alertasPorPaciente);
    }
}
