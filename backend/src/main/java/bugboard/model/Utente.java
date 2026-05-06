package bugboard.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "utente", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Il dump usa 'integer'

    @Column(nullable = false, unique = true, length = 255)
    private String email; // 'character varying(255)'

    @Column(nullable = false, length = 255)
    @ToString.Exclude // Escludiamo la password nei log per sicurezza
    private String password; // 'character varying(255)'

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.OTHER) // Assicura che l'ENUM sia trattato come tipo personalizzato
    @Column(name = "role", nullable = false, columnDefinition = "utenteruolo")
    private Role role; // Il tipo ENUM definito nel dump

    @Column(nullable = false, columnDefinition = "varchar(100)")
    private String name;

    @Column(nullable = false, columnDefinition = "varchar(100)")
    private String surname;

    // Relazioni inverse (opzionali ma utili per navigare i dati in Java)
    @JsonIgnore
    @OneToMany(mappedBy = "creatore")
    private List<Issue> issuesCreate;
    @JsonIgnore
    @OneToMany(mappedBy = "assegnatario")
    private List<Issue> issuesAssegnate;

    // L'Enum deve riflettere i valori del database[cite: 1]
    // Se hai rinominato i valori via SQL come abbiamo fatto prima, 
    // usa ADMIN e USER. Se hai lasciato il dump originale, usa quelli sotto.
    public enum Role {
        ADMIN,          // Se hai fatto l'ALTER TYPE
        USER,           // Se hai fatto l'ALTER TYPE
        READONLY        // Valore originale nel dump[cite: 1]
    }
}
