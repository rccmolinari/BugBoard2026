package bugboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import bugboard.model.Commento;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueSpecific {
      private Integer id;
      private String titolo;
      private String descrizione;
      private byte[] immagine;
      private String immagineContentType;
      private Integer priorita;
      private List<Commento> commento;
      private String[] etichetta;
      private String tipo;
      private String stato;
      private String emailAssegnatario;
      private LocalDateTime dataScadenza;     
      private LocalDateTime dataCreazione;
}
