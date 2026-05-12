package bugboard.model;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sessione", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sessione {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID sid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid", nullable = false)
    private Utente utente;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}