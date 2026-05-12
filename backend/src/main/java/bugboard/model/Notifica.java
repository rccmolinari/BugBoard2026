package bugboard.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifica", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Notifica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "idissue", nullable = false)
    private Issue issue;

    @ManyToOne
    @JoinColumn(name = "idassegnatario", nullable = false)
    private Utente assegnatario;

    @ManyToOne
    @JoinColumn(name = "idassegnato_a", nullable = false)
    private Utente assegnatoA;

    @Column(name = "letta", nullable = false, columnDefinition = "boolean default false")
    private boolean letta = false;
    
    @Column(name = "datacreazione", nullable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime dataCreazione = LocalDateTime.now();
    
}
