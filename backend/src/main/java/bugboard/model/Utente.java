package bugboard.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import com.fasterxml.jackson.annotation.JsonIgnore;


import java.util.List;

@Entity
@Table(name = "utente", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

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
    private List<Issue> issuesAssegnate;
    @JsonIgnore
    @OneToMany(mappedBy = "assegnatoA")
    private List<Issue> issuesAssegnatoA;

    public enum Role {
        ADMIN, USER, READONLY;

        public static Role fromValue(String value) {
            for (Role r : Role.values()) {
                if (r.name().equalsIgnoreCase(value)) return r;
            }
            throw new IllegalArgumentException("Tipo non valido: " + value);
        }

        /*
         * OCP + SRP — la logica di mapping verso il formato frontend
         * risiede nell'enum stesso. Aggiungere un nuovo ruolo richiede
         * solo di aggiungere un caso qui, senza toccare AuthService.
         */
        public String toFrontendRole() {
            switch (this) {
                case ADMIN:    return "admin";
                case READONLY: return "readonly";
                case USER:     return "normal";
                default:       return this.name().toLowerCase();
            }
        }
    }
}
