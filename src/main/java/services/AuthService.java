package services;

import dao.PessoaDAO;
import model.Pessoa;
import utils.Hasher;

import java.sql.Date;
import java.sql.SQLException;

public class AuthService {

    public Pessoa cadastrar(String email, String senha, String nome, String telefone, Date data_nascimento)
            throws SQLException {
        PessoaDAO dao = new PessoaDAO();
        if (dao.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
        String senhaHash = Hasher.hashPassword(senha);
        Pessoa pessoa = new Pessoa(0, nome, data_nascimento, email, senhaHash, telefone);
        dao.insert(pessoa);
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
