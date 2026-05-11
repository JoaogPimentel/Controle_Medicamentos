package services;

import dao.CuidadorDAO;
import dao.PacienteDAO;
import dao.PessoaDAO;
import model.Cuidador;
import model.Paciente;
import model.Pessoa;
import model.RolePessoa;
import utils.Hasher;

import java.sql.Date;
import java.sql.SQLException;

public class AuthService {

    public Pessoa cadastrar(String email, String senha, String nome, String telefone, Date data_nascimento,
                            RolePessoa role, boolean profissionalSaude, String registroProfissional)
            throws SQLException {
        PessoaDAO pessoaDAO = new PessoaDAO();
        if (pessoaDAO.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        if (role == RolePessoa.CUIDADOR
                && profissionalSaude
                && (registroProfissional == null || registroProfissional.isBlank())) {
            throw new IllegalArgumentException("Registro profissional é obrigatório para profissionais de saúde.");
        }

        String senhaHash = Hasher.hashPassword(senha);
        Pessoa pessoa = new Pessoa(0, nome, data_nascimento, email, senhaHash, telefone);
        pessoaDAO.insert(pessoa);

        if (role == RolePessoa.CUIDADOR) {
            Cuidador cuidador = new Cuidador();
            cuidador.setId_pessoa(pessoa.getId_pessoa());
            cuidador.setProfissional_saude(profissionalSaude);
            cuidador.setRegistro_profissional(registroProfissional);
            new CuidadorDAO().insert(cuidador);
        } else {
            Paciente paciente = new Paciente();
            paciente.setId_pessoa(pessoa.getId_pessoa());
            new PacienteDAO().insert(paciente);
        }

        return pessoa;
    }

    public Pessoa login(String email, String senha) throws SQLException {
        PessoaDAO dao = new PessoaDAO();
        Pessoa pessoa = dao.findByEmail(email);
        if (pessoa == null || !Hasher.verifyPassword(senha, pessoa.getSenha_hash())) {
            throw new IllegalArgumentException("Email ou senha inválidos");
        }
        return pessoa;
    }
}
