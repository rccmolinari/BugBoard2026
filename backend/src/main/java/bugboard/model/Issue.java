package bugboard.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.BinaryJdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "issue", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String titolo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descrizione;

    private Integer priorita;
    
    @Column(name = "immagine", columnDefinition = "bytea")
    @JdbcType(BinaryJdbcType.class)
    private byte[] immagine;

    @Column(name = "immagine_content_type", length = 50)
    private String immagineContentType;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, columnDefinition = "issuetipo")
    private TipoIssue tipo;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, columnDefinition = "issuestato")
    private StatoIssue stato = StatoIssue.TODO;

    @Column(name = "datascadenza")
    private LocalDateTime dataScadenza;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "etichetta", columnDefinition = "text[]")
    private String[] etichetta;


    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "commento", columnDefinition = "jsonb")
    private List<Commento> commento = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "idcreatore", nullable = false)
    private Utente creatore;

    @ManyToOne
    @JoinColumn(name = "idassegnatario")
    private Utente assegnatario;

    @ManyToOne
    @JoinColumn(name = "assegnato_a")
    private Utente assegnatoA;

    // Gli Enum devono corrispondere esattamente al dump
    public enum StatoIssue { 
        TODO, IN_PROGRESS, DONE, EXPIRED;

        public static StatoIssue fromValue(String value) {
            for (StatoIssue s : StatoIssue.values()) {
                if (s.name().equalsIgnoreCase(value)) return s;
            }
            throw new IllegalArgumentException("Stato non valido: " + value);
        }
    }

    public enum TipoIssue { 
        QUESTION, BUG, DOCUMENTATION, FEATURE;

        public static TipoIssue fromValue(String value) {
            for (TipoIssue t : TipoIssue.values()) {
                if (t.name().equalsIgnoreCase(value)) return t;
            }
            throw new IllegalArgumentException("Tipo non valido: " + value);
        }
    }
}
