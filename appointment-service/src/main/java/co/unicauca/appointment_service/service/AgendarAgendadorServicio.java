package co.unicauca.appointment_service.service;

import co.unicauca.appointment_service.dto.AgendarAgendadorRequest;
import co.unicauca.appointment_service.model.Cita;
import co.unicauca.appointment_service.model.EstadoCita;
import co.unicauca.appointment_service.repository.CitaRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 *
 * @author Juan Martin
 */
@Service
public class AgendarAgendadorServicio {
    private final CitaRepository repo;

    public AgendarAgendadorServicio(CitaRepository repo) {
        this.repo = repo;
    }

    public Cita agendar(AgendarAgendadorRequest req) {

        Cita cita = new Cita(
                UUID.randomUUID().toString(),
                req.getPacienteId(),
                req.getNombrePaciente(),
                req.getEspecialistaId(),
                "Especialista",
                req.getFecha(),
                req.getHora(),
                60,
                EstadoCita.AGENDADA
        );

        return repo.save(cita);
    }
}
