package bugboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// I campi per creare un utente, buoni sia per la registrazione sia per l'admin.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String email;
    private String password;
    private String name;
    private String surname;
    private String role; // "user", "admin" oppure "readonly"
}
