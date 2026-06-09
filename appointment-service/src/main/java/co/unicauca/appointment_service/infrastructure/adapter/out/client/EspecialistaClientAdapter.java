package co.unicauca.appointment_service.infrastructure.adapter.out.client;

import co.unicauca.appointment_service.domain.port.out.EspecialistaServicePort;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Adaptador de salida: llama al especialista-service para obtener datos de un especialista.
 */
@Component
public class EspecialistaClientAdapter implements EspecialistaServicePort {

    private final RestTemplate restTemplate;

    @Value("${services.especialista.url:http://localhost:8081}")
    private String especialistaServiceUrl;

    public EspecialistaClientAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DatosEspecialista obtener(String especialistaId) {
        Map<String, Object> r = restTemplate.getForObject(
                especialistaServiceUrl + "/especialistas/" + especialistaId, Map.class);
        if (r == null) throw new IllegalStateException("Especialista no encontrado: " + especialistaId);
        return new DatosEspecialista(
                especialistaId,
                String.valueOf(r.getOrDefault("nombre", "Especialista")),
                String.valueOf(r.getOrDefault("especialidad", "")));
    }
}
