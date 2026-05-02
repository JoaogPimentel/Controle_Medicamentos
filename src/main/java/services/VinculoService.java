package services;

import dao.CuidadorDAO;
import dao.PacienteCuidadorDAO;
import dao.PacienteDAO;
import model.PacienteCuidador;

import java.sql.SQLException;
import java.util.List;

public class VinculoService {

    private final PacienteDAO pacienteDAO       = new PacienteDAO();
    private final CuidadorDAO cuidadorDAO       = new CuidadorDAO();
    private final PacienteCuidadorDAO pcDAO     = new PacienteCuidadorDAO();

    public PacienteCuidador vincular(int idPaciente, int idCuidador) throws SQLException {
        if (pacienteDAO.findById(idPaciente) == null) {
            throw new IllegalArgumentException("Paciente não encontrado.");
        }
        if (cuidadorDAO.findById(idCuidador) == null) {
            throw new IllegalArgumentException("Cuidador não encontrado.");
        }

        List<PacienteCuidador> ativos = pcDAO.findAtivosByPaciente(idPaciente);
        for (PacienteCuidador v : ativos) {
            if (v.getId_cuidador() == idCuidador) {
                throw new IllegalArgumentException("Vínculo já está ativo entre este paciente e cuidador.");
            }
        }

        PacienteCuidador vinculo = new PacienteCuidador();
        vinculo.setId_paciente(idPaciente);
        vinculo.setId_cuidador(idCuidador);
        vinculo.setAtivo(true);
        pcDAO.insert(vinculo);
        return vinculo;
    }

    public List<PacienteCuidador> listarPorPaciente(int idPaciente) throws SQLException {
        return pcDAO.findAtivosByPaciente(idPaciente);
    }

    public List<PacienteCuidador> listarPorCuidador(int idCuidador) throws SQLException {
        return pcDAO.findAtivosByCuidador(idCuidador);
    }

    public void desvincular(int idVinculo) throws SQLException {
        PacienteCuidador vinculo = pcDAO.findById(idVinculo);
        if (vinculo == null) {
            throw new IllegalArgumentException("Vínculo não encontrado.");
        }
        if (!vinculo.isAtivo()) {
            throw new IllegalArgumentException("Vínculo já está encerrado.");
        }
        pcDAO.encerrarVinculo(idVinculo);
    }
}
