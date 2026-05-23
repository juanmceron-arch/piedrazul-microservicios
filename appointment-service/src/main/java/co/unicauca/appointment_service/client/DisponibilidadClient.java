package co.unicauca.appointment_service.client;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DisponibilidadClient {
    private final RestTemplate restTemplate;

    public DisponibilidadClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerDisponibilidad(String especialistaId) {
        return restTemplate.getForObject(
                "http://localhost:8081/disponibilidad/" + especialistaId,
                Map.class
        );
    }
}
