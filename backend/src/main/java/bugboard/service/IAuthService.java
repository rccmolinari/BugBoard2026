package bugboard.service;

import bugboard.dto.AuthResponse;
import bugboard.dto.LoginRequest;
import bugboard.dto.RegisterRequest;

/*
 * DIP — i controller dipendono da questa astrazione, non dalla classe concreta AuthService.
 */
public interface IAuthService {
    AuthResponse login(LoginRequest request);
    boolean register(RegisterRequest request);
}
