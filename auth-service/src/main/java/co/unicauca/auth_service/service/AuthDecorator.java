package co.unicauca.auth_service.service;

import co.unicauca.auth_service.DTO.AuthResponse;
import co.unicauca.auth_service.DTO.LoginRequest;
import co.unicauca.auth_service.DTO.PacienteResponse;
import co.unicauca.auth_service.DTO.RegisterRequest;
import java.util.List;


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
    public PacienteResponse obtenerPaciente(Integer id) {
        return authService.obtenerPaciente(id);
    }

    @Override
    public List<PacienteResponse> buscarPacientes(String documento) {
        return authService.buscarPacientes(documento);
    }
    
}
