package bugboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
// La risposta a un login andato a buon fine: id di sessione, nome e ruolo.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
        private String sessionId;
        private String nome;
        private String ruolo;
}
