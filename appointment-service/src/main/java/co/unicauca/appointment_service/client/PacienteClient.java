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
public class PacienteClient {
    private final RestTemplate restTemplate;
    @Value("${services.auth.url:http://localhost:8080}")
    private String authServiceUrl;

    public PacienteClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerPaciente(int id) {
        return restTemplate.getForObject(
                authServiceUrl + "/auth/pacientes/" + id,
                Map.class
        );
    }
}
