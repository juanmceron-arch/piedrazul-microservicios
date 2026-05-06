package co.unicauca.auth_service.service;

import co.unicauca.auth_service.DTO.AuthResponse;
import co.unicauca.auth_service.DTO.LoginRequest;
import co.unicauca.auth_service.DTO.RegisterRequest;

/**
 *
 * @author Juan Martin
 */
public interface AuthService {
    
    AuthResponse registrar(RegisterRequest request);

    AuthResponse login(LoginRequest request);
    
}
