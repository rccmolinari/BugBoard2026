package bugboard.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Riga sintetica di una issue per le liste di admin e stakeholder.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueResponse {
    private Integer id;
    private String titolo;
    private String tipo;
    private Integer priorita;
    private String stato;
    private String creatore;
    private String assegnatoA;
    private LocalDateTime dataScadenza;
    private LocalDateTime dataCreazione;
    private Long version;
}