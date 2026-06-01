package co.unicauca.appointment_service.client;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DisponibilidadClient {
    private final RestTemplate restTemplate;
    @Value("${services.especialista.url:http://localhost:8081}")
    private String especialistaServiceUrl;

    public DisponibilidadClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerDisponibilidad(String especialistaId) {
        return restTemplate.getForObject(
                especialistaServiceUrl + "/disponibilidad/" + especialistaId,
                Map.class
        );
    }
}
