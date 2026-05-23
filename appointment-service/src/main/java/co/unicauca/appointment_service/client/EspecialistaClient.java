package co.unicauca.appointment_service.client;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Juan Martin
 */
@Component
public class EspecialistaClient {
    private final RestTemplate restTemplate;

    public EspecialistaClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerEspecialista(String id) {
        return restTemplate.getForObject(
                "http://localhost:8081/especialistas/" + id,
                Map.class
        );
    }
}
