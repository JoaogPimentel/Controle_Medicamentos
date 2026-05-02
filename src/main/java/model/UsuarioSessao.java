package model;

import java.io.Serializable;

/**
 * Objeto armazenado na HttpSession após login bem-sucedido.
 * Serializable permite que o servidor persista/replique a sessão.
 */
public class UsuarioSessao implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int         idPessoa;
    private final String      nome;
    private final String      email;
    private final RolePessoa  papel;

    public UsuarioSessao(int idPessoa, String nome, String email, RolePessoa papel) {
        this.idPessoa = idPessoa;
        this.nome     = nome;
        this.email    = email;
        this.papel    = papel;
    }

    public int        getIdPessoa() { return idPessoa; }
    public String     getNome()     { return nome; }
    public String     getEmail()    { return email; }
    public RolePessoa getPapel()    { return papel; }
}
