package bugboard.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserRegistrationRequest {
             
    private String email;
    private String password;
    private String name;
    private String surname;
    private String role; // user admin o readonly
}