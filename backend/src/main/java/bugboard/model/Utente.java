package bugboard.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "utente", schema = "public")
@Data
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String name; // Mappa la colonna 'name' nel DB

    @Column(nullable = false, length = 100)
    private String surname; // Mappa la colonna 'surname' nel DB

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "role", nullable = false, columnDefinition = "utenteruolo")
    private Role role;
    @JsonIgnore
    @OneToMany(mappedBy = "creatore")
    private List<Issue> issuesCreate;

    @JsonIgnore
    @OneToMany(mappedBy = "assegnatario")
    private List<Issue> issuesAssegnatario;

    @JsonIgnore
    @OneToMany(mappedBy = "assegnatoA")
    private List<Issue> issuesAssegnatoA;

    public enum Role {
        ADMIN, 
        USER, 
        READONLY
    }
}
