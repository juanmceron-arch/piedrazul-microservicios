package co.unicauca.auth_service.controller;

import co.unicauca.auth_service.DTO.AuthResponse;
import co.unicauca.auth_service.DTO.LoginRequest;
import co.unicauca.auth_service.DTO.PacienteResponse;
import co.unicauca.auth_service.DTO.RegisterRequest;
import co.unicauca.auth_service.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return service.registrar(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return service.login(request);
    }
    
    @GetMapping("/pacientes/{id}")
    public PacienteResponse obtenerPaciente(@PathVariable Integer id) {
        return service.obtenerPaciente(id);
    }

    @GetMapping("/pacientes")
    public List<PacienteResponse> buscarPacientes(@RequestParam String documento) {
        return service.buscarPacientes(documento);
    }
}
