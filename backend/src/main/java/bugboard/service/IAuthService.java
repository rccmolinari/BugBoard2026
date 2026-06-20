package bugboard.service;

import bugboard.dto.AuthResponse;
import bugboard.dto.LoginRequest;
import bugboard.dto.RegisterRequest;

/*
 * Il contratto per login e registrazione: il controller si appoggia a questo
 * e non gli importa quale implementazione ci sia davvero dietro.
 */
public interface IAuthService {
    AuthResponse login(LoginRequest request);
    void register(RegisterRequest request);
}
