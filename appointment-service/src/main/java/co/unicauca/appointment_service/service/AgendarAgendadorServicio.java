package co.unicauca.appointment_service.service;

import co.unicauca.appointment_service.client.EspecialistaClient;
import co.unicauca.appointment_service.dto.AgendarAgendadorRequest;
import co.unicauca.appointment_service.model.Cita;
import co.unicauca.appointment_service.model.EstadoCita;
import co.unicauca.appointment_service.repository.CitaRepository;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 *
 * @author Juan Martin
 */
@Service
public class AgendarAgendadorServicio {
    private final CitaRepository repo;
    private final EspecialistaClient especialistaClient;

    public AgendarAgendadorServicio(CitaRepository repo, EspecialistaClient especialistaClient) {
        this.repo = repo;
        this.especialistaClient = especialistaClient;
    }

    public Cita agendar(AgendarAgendadorRequest req) {
        validarFecha(req.getFecha());
        if (repo.existsByEspecialistaIdAndFechaAndHoraAndEstadoNot(req.getEspecialistaId(), req.getFecha(), req.getHora(), EstadoCita.CANCELADA)) {
            throw new RuntimeException("Horario ocupado");
        }

        Map<String, Object> especialista = especialistaClient.obtenerEspecialista(req.getEspecialistaId());

        Cita cita = new Cita(
                UUID.randomUUID().toString(),
                req.getPacienteId(),
                nombrePaciente(req),
                req.getEspecialistaId(),
                valorComoTexto(especialista, "nombre", "Especialista"),
                req.getFecha(),
                req.getHora(),
                60,
                EstadoCita.AGENDADA
        );

        return repo.save(cita);
    }

    private void validarFecha(LocalDate fecha) {
        if (fecha != null && fecha.isBefore(LocalDate.now())) {
            throw new RuntimeException("No se pueden agendar citas en fechas pasadas");
        }
    }

    private String nombrePaciente(AgendarAgendadorRequest req) {
        return (String.valueOf(req.getNombrePaciente() == null ? "" : req.getNombrePaciente()) + " "
                + String.valueOf(req.getApellidoPaciente() == null ? "" : req.getApellidoPaciente())).trim();
    }

    private String valorComoTexto(Map<String, Object> map, String key, String fallback) {
        if (map == null || map.get(key) == null) return fallback;
        return String.valueOf(map.get(key));
    }
}
