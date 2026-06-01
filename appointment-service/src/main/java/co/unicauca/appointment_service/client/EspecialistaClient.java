package co.unicauca.appointment_service.client;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Juan Martin
 */
@Component
public class EspecialistaClient {
    private final RestTemplate restTemplate;
    @Value("${services.especialista.url:http://localhost:8081}")
    private String especialistaServiceUrl;

    public EspecialistaClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerEspecialista(String id) {
        return restTemplate.getForObject(
                especialistaServiceUrl + "/especialistas/" + id,
                Map.class
        );
    }
}
