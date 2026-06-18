package bugboard.service;

import bugboard.dto.AllUserResponse;
import bugboard.dto.RegisterRequest;

import java.util.List;
import java.util.UUID;

/*
 * DIP — UserController dipende da questa astrazione, non da UserService direttamente.
 */
public interface IUserService {
    AllUserResponse creaNuovoUtente(UUID sid, RegisterRequest request);
    List<AllUserResponse> getAllUsers(UUID sid);
    void deleteUser(UUID sid, String email);
}
