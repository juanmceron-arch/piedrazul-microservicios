package co.unicauca.appointment_service.client;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Objeto real (RealSubject) del patron Proxy.
 *
 * Realiza la invocacion HTTP efectiva al especialista-service.
 *
 * @author Juan Martin
 */
@Component
public class EspecialistaClient implements EspecialistaGateway {
    private final RestTemplate restTemplate;

    public EspecialistaClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerEspecialista(String id) {
        return restTemplate.getForObject(
                "http://localhost:8081/especialistas/" + id,
                Map.class
        );
    }
}
