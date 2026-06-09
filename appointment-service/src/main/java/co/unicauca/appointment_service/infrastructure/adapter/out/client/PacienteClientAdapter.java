package co.unicauca.appointment_service.infrastructure.adapter.out.client;

import co.unicauca.appointment_service.domain.port.out.PacienteServicePort;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Adaptador de salida: llama al auth-service para obtener datos de un paciente.
 */
@Component
public class PacienteClientAdapter implements PacienteServicePort {

    private final RestTemplate restTemplate;

    @Value("${services.auth.url:http://localhost:8080}")
    private String authServiceUrl;

    public PacienteClientAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DatosPaciente obtener(int pacienteId) {
        Map<String, Object> r = restTemplate.getForObject(
                authServiceUrl + "/auth/pacientes/" + pacienteId, Map.class);
        if (r == null) throw new IllegalStateException("Paciente no encontrado: " + pacienteId);
        return new DatosPaciente(
                pacienteId,
                str(r, "nombre"),
                str(r, "apellido"),
                str(r, "telefono"),
                fecha(r, "fechaNacimiento"),
                str(r, "correo"),
                str(r, "genero"));
    }

    private String str(Map<String, Object> m, String k) {
        return m.get(k) == null ? "" : String.valueOf(m.get(k));
    }

    private LocalDate fecha(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v == null || String.valueOf(v).isBlank()) return null;
        return LocalDate.parse(String.valueOf(v));
    }
}
