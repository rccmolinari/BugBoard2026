package bugboard.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "issue", schema = "public")
@Data
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String titolo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descrizione;

    private Integer priorita;

    @Column(length = 500)
    private String immagine;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "issuetipo")
    private TipoIssue tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "issuestato")
    private StatoIssue stato = StatoIssue.TODO;

    @Column(name = "datascadenza")
    private LocalDateTime dataScadenza;

    // Mapping corretto per text[] di PostgreSQL
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] etichetta;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] commento;

    @ManyToOne
    @JoinColumn(name = "idcreatore", nullable = false)
    private Utente creatore;

    @ManyToOne
    @JoinColumn(name = "idassegnatario")
    private Utente assegnatario;

    @ManyToOne
    @JoinColumn(name = "assegnato_a") // Questo deve corrispondere al nome nel DB
    private Utente assegnatoA;

    public enum StatoIssue { TODO, IN_PROGRESS, DONE }
    public enum TipoIssue { QUESTION, BUG, DOCUMENTATION, FEATURE }
}