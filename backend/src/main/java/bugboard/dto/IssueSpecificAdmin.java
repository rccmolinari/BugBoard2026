package bugboard.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;





@NoArgsConstructor
@AllArgsConstructor
@Data
public class IssueSpecificAdmin {
        private Integer id;
        private String titolo;
        private String descrizione;
        private byte[] immagine;
        private String immagineContentType;
        private Integer priorita;
        private LocalDateTime dataScadenza;
        private String[] commento;
        private String[] etichetta;
        private String tipo;
        private String stato;
        private String emailCreatore;
        private String emailAssegnatario;
        private String emailAssegnatoA;        
}
