package bugboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
      private String[] commento;
      private String[] etichetta;
      private String tipo;
      private String stato;
      private String emailAssegnatario;
      private LocalDateTime dataScadenza;     
}
