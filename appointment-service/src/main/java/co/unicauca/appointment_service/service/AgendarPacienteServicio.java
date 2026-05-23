package co.unicauca.appointment_service.service;

import co.unicauca.appointment_service.client.EspecialistaClient;
import co.unicauca.appointment_service.client.PacienteClient;
import co.unicauca.appointment_service.dto.AgendarPacienteRequest;
import co.unicauca.appointment_service.builder.CitaAgendadaBuilder;
import co.unicauca.appointment_service.model.Cita;
import co.unicauca.appointment_service.model.EstadoCita;
import co.unicauca.appointment_service.repository.CitaRepository;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 *
 * @author Juan Martin
 */
@Service
public class AgendarPacienteServicio {
    private final CitaRepository repo;
    private final PacienteClient pacienteClient;
    private final EspecialistaClient especialistaClient;

    public AgendarPacienteServicio(CitaRepository repo, PacienteClient pacienteClient, EspecialistaClient especialistaClient) {
        this.repo = repo;
        this.pacienteClient = pacienteClient;
        this.especialistaClient = especialistaClient;
    }

    public Cita agendar(AgendarPacienteRequest req) {
        validarFecha(req.getFecha());
        validarHorarioLibre(req.getEspecialistaId(), req.getFecha(), req.getHora());

        Map<String, Object> paciente = pacienteClient.obtenerPaciente(req.getPacienteId());
        Map<String, Object> especialista = especialistaClient.obtenerEspecialista(req.getEspecialistaId());

        Cita cita = new CitaAgendadaBuilder()
                .conPaciente(req.getPacienteId(), nombreCompleto(paciente))
                .conEspecialista(req.getEspecialistaId(), valorComoTexto(especialista, "nombre", "Especialista"))
                .conFecha(req.getFecha())
                .conHora(req.getHora())
                .build();

        return repo.save(cita);
    }

    private void validarHorarioLibre(String especialistaId, LocalDate fecha, java.time.LocalTime hora) {
        if (repo.existsByEspecialistaIdAndFechaAndHoraAndEstadoNot(especialistaId, fecha, hora, EstadoCita.CANCELADA)) {
            throw new RuntimeException("Horario ocupado");
        }
    }

    private void validarFecha(LocalDate fecha) {
        if (fecha != null && fecha.isBefore(LocalDate.now())) {
            throw new RuntimeException("No se pueden agendar citas en fechas pasadas");
        }
    }

    private String nombreCompleto(Map<String, Object> paciente) {
        String nombre = valorComoTexto(paciente, "nombre", "Paciente");
        String apellido = valorComoTexto(paciente, "apellido", "");
        return (nombre + " " + apellido).trim();
    }

    private String valorComoTexto(Map<String, Object> map, String key, String fallback) {
        if (map == null || map.get(key) == null) return fallback;
        return String.valueOf(map.get(key));
    }
}
