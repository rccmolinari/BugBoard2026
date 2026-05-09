package bugboard.dto;

public class AuthResponse {
    private Integer id;
    private String nome;
    private String ruolo;

    public AuthResponse(Integer id, String nome, String ruolo) {
        this.id = id;
        this.nome = nome;
        this.ruolo = ruolo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getRuolo() {
        return ruolo;
    }

    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }
}
