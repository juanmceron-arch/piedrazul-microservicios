package co.unicauca.appointment_service.infrastructure.adapter.out.client;

import co.unicauca.appointment_service.domain.port.out.DisponibilidadServicePort;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Adaptador de salida: llama al especialista-service para obtener disponibilidad.
 */
@Component
public class DisponibilidadClientAdapter implements DisponibilidadServicePort {

    private final RestTemplate restTemplate;

    @Value("${services.especialista.url:http://localhost:8081}")
    private String especialistaServiceUrl;

    public DisponibilidadClientAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Disponibilidad obtener(String especialistaId) {
        Map<String, Object> r = restTemplate.getForObject(
                especialistaServiceUrl + "/disponibilidad/" + especialistaId, Map.class);
        if (r == null) return null;

        List<DayOfWeek> dias = List.of();
        Object diasRaw = r.get("diasAtencion");
        if (diasRaw instanceof List<?> lista) {
            dias = lista.stream()
                    .map(d -> DayOfWeek.valueOf(String.valueOf(d).toUpperCase()))
                    .toList();
        }

        LocalTime inicio = LocalTime.parse(String.valueOf(r.get("horaInicio")));
        LocalTime fin    = LocalTime.parse(String.valueOf(r.get("horaFin")));
        int intervalo    = numero(r.get("intervaloMinutos"), 60);
        int semanas      = numero(r.get("semanasHabilitadas"), 0);

        return new Disponibilidad(dias, inicio, fin, intervalo, semanas);
    }

    private int numero(Object v, int fallback) {
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return fallback; }
    }
}
