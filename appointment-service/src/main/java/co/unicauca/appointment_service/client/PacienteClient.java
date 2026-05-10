package co.unicauca.appointment_service.client;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Juan Martin
 */
@Component
public class PacienteClient {
    private final RestTemplate restTemplate;

    public PacienteClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerPaciente(int id) {
        return restTemplate.getForObject(
                "http://localhost:8080/auth/pacientes/" + id,
                Map.class
        );
    }
}
