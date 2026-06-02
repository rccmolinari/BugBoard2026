package bugboard.service;

import bugboard.dto.AllUserResponse;
import bugboard.dto.RegisterRequest;
import bugboard.model.Utente;

import java.util.List;
import java.util.UUID;

/*
 * DIP — UserController dipende da questa astrazione, non da UserService direttamente.
 */
public interface IUserService {
    Utente creaNuovoUtente(UUID sid, RegisterRequest request);
    List<AllUserResponse> getAllUsers(UUID sid);
    boolean deleteUser(UUID sid, String email);
}
