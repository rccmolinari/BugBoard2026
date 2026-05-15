package bugboard.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueResponseUser {

    private Integer id;

    private String titolo;
    
    private String tipo;

    private Integer priorita;

    private String stato;

    private String assegnatoDa;

    private LocalDateTime dataScadenza;
    
}