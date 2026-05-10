package co.unicauca.auth_service.service;

import co.unicauca.auth_service.DTO.AuthResponse;
import co.unicauca.auth_service.DTO.LoginRequest;
import co.unicauca.auth_service.DTO.RegisterRequest;
import co.unicauca.auth_service.model.Usuario;

public interface AuthService {

    AuthResponse registrar(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    Usuario obtenerPaciente(Integer id);
}
