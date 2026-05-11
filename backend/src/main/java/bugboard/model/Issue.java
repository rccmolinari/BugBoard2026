package bugboard.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "issue", schema = "public")
@Getter
@Setter
@NoArgsConstructor
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
    private String immagine; // Mappa 'immagine character varying(500)'

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.OTHER) // Assicura che l'ENUM sia trattato come tipo personalizzato
    @Column(nullable = false, columnDefinition = "issuetipo")
    private TipoIssue tipo;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.OTHER) // Assicura che l'ENUM sia trattato come tipo personalizzato
    @Column(nullable = false, columnDefinition = "issuestato")
    private StatoIssue stato = StatoIssue.TODO;

    @Column(name = "datascadenza")
    private LocalDateTime dataScadenza;

    // Per etichetta e commento (text[]), in Spring si usano le liste o array semplici
    @JdbcTypeCode(SqlTypes.ARRAY) // Assicura che sia trattato come array    
    @Column(name = "etichetta", columnDefinition = "text[]") // Specifica il tipo array nel database
    private String[] etichetta;
    
    @JdbcTypeCode(SqlTypes.ARRAY) // Assicura che sia trattato come array
    @Column(name = "commento", columnDefinition = "text[]") // Specifica il tipo array nel database
    private String[] commento;

    @ManyToOne
    @JoinColumn(name = "idcreatore", nullable = false)
    private Utente creatore;

    @ManyToOne
    @JoinColumn(name = "idassegnatario")
    private Utente assegnatario;

    @ManyToOne
    @JoinColumn(name = "assegnatoA")
    private Utente assegnatoA;

    // Gli Enum devono corrispondere esattamente al dump
    public enum StatoIssue { TODO, IN_PROGRESS, DONE }
    public enum TipoIssue { QUESTION, BUG, DOCUMENTATION, FEATURE }
}
