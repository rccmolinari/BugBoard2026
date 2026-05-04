package bugboard.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "issue", schema = "public")
@Data
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Nel dump è un integer

    @Column(nullable = false)
    private String titolo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descrizione;

    private Integer priorita;

    private String immagine; // Mappa 'immagine character varying(500)'

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "issuetipo")
    private TipoIssue tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "issuestato")
    private StatoIssue stato = StatoIssue.TODO; // Il dump ha DEFAULT 'TODO'

    @Column(name = "datascadenza")
    private LocalDateTime dataScadenza;

    // Per etichetta e commento (text[]), in Spring si usano le liste o array semplici
    private String[] etichetta;
    private String[] commento;

    @ManyToOne
    @JoinColumn(name = "idcreatore", nullable = false)
    private Utente creatore;

    @ManyToOne
    @JoinColumn(name = "idassegnatario")
    private Utente assegnatario;

    // Gli Enum devono corrispondere esattamente al dump
    public enum StatoIssue { TODO, IN_PROGRESS, DONE }
    public enum TipoIssue { QUESTION, BUG, DOCUMENTATION, FEATURE }
}