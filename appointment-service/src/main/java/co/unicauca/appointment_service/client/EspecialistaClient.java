package co.unicauca.appointment_service.client;

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

    public Object obtenerEspecialista(String id) {
        return restTemplate.getForObject(
                "http://localhost:8080/auth/especialistas/" + id,
                Object.class
        );
    }
}
