package co.unicauca.auth_service.service;

import co.unicauca.auth_service.DTO.AuthResponse;
import co.unicauca.auth_service.DTO.LoginRequest;
import co.unicauca.auth_service.DTO.PacienteResponse;
import co.unicauca.auth_service.DTO.RegisterRequest;
import java.util.List;

public interface AuthService {

    AuthResponse registrar(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    PacienteResponse obtenerPaciente(Integer id);

    List<PacienteResponse> buscarPacientes(String documento);
}
