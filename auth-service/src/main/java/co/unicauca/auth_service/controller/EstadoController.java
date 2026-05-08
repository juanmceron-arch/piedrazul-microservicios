package co.unicauca.auth_service.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth-service")
public class EstadoController {

    @GetMapping("/estado")
    public Map<String, String> estado() {
        return Map.of(
                "servicio", "auth-service",
                "estado", "activo"
        );
    }
}
