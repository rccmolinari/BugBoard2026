package bugboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data //genera metodi getter setter tostring e equals
@NoArgsConstructor //genera costruttore vuoto
@AllArgsConstructor //genera costruttore con tutti i campi

public class LoginRequest {
    private String email;
    private String password;  
}
