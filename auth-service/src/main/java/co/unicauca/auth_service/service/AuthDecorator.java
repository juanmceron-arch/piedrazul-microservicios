package co.unicauca.auth_service.service;

import co.unicauca.auth_service.DTO.AuthResponse;
import co.unicauca.auth_service.DTO.LoginRequest;
import co.unicauca.auth_service.DTO.RegisterRequest;
import co.unicauca.auth_service.model.Usuario;


public abstract class AuthDecorator implements AuthService{

    protected final AuthService authService;

    public AuthDecorator(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public AuthResponse registrar(RegisterRequest request) {
        return authService.registrar(request);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        return authService.login(request);
    }

    @Override
    public Usuario obtenerPaciente(Integer id) {
        return authService.obtenerPaciente(id);
    }
    
}
