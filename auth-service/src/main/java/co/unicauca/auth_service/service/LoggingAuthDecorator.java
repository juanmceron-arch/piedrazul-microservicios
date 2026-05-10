package co.unicauca.auth_service.service;

import co.unicauca.auth_service.DTO.AuthResponse;
import co.unicauca.auth_service.DTO.LoginRequest;
import co.unicauca.auth_service.DTO.RegisterRequest;


public class LoggingAuthDecorator extends AuthDecorator {
    
    public LoggingAuthDecorator(AuthService authService) {
        super(authService);
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        System.out.println("Intento login usuario: " + request.getId());

        AuthResponse response = super.login(request);

        System.out.println("Resultado: " + response.getMensaje());

        return response;
    }

    @Override
    public AuthResponse registrar(RegisterRequest request) {

        System.out.println("Registro usuario: " + request.getId());

        return super.registrar(request);
    }
}
