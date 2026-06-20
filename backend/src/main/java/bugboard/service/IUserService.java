package bugboard.service;

import bugboard.dto.AllUserResponse;
import bugboard.dto.RegisterRequest;

import java.util.List;
import java.util.UUID;

/*
 * Quello che l'admin può fare sugli utenti: crearne di nuovi, vederli tutti
 * o eliminarli. Il controller dipende da qui e non dalla classe concreta.
 */
public interface IUserService {
    AllUserResponse creaNuovoUtente(UUID sid, RegisterRequest request);
    List<AllUserResponse> getAllUsers(UUID sid);
    void deleteUser(UUID sid, String email);
}
