package bugboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Una notifica già pronta per il frontend, col messaggio bell'e scritto.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notify {
    private Integer id;
    private String messaggio;
    private LocalDateTime dataCreazione;
    private boolean letta;
    private Integer issueId;
    private String titoloIssue;
}
