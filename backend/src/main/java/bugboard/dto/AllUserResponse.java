package bugboard.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// L'utente come lo vede l'admin nella tabella, password esclusa.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllUserResponse {
    private Integer id;
    private String name;
    private String surname;
    private String email;
    private String role;
    
}
