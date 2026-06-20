package bugboard.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import bugboard.model.Commento;





// Il dettaglio di una issue lato admin: come quello utente, ma con tutte le email coinvolte.
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
        private List<Commento> commento;
        private String[] etichetta;
        private String tipo;
        private String stato;
        private String emailCreatore;
        private String emailAssegnatario;
        private String emailAssegnatoA;    
        private LocalDateTime dataCreazione;    
}
