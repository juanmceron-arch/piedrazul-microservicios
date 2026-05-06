package co.unicauca.auth_service.service;

import co.unicauca.auth_service.DTO.AuthResponse;
import co.unicauca.auth_service.DTO.RegisterRequest;

/**
 *
 * @author Juan Martin
 */
public class ValidationAuthDecorator extends AuthDecorator{
    public ValidationAuthDecorator(AuthService authService) {
        super(authService);
    }

    @Override
    public AuthResponse registrar(RegisterRequest request) {

        if (request.getId() <= 0) {
            return new AuthResponse( "ID inválido",0,null,false);
        }

        if (request.getPasswordHash()== null || request.getPasswordHash().isBlank()) {
            return new AuthResponse("Contraseña requerida", 0,null,false);
        }

        return super.registrar(request);
    }
}
