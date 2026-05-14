package bugboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
