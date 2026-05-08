package co.unicauca.appointment_service.service;

import co.unicauca.appointment_service.client.EspecialistaClient;
import co.unicauca.appointment_service.client.PacienteClient;
import co.unicauca.appointment_service.dto.AgendarPacienteRequest;
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
public class AgendarPacienteServicio {
    private final CitaRepository repo;
    private final PacienteClient pacienteClient;
    private final EspecialistaClient especialistaClient;

    public AgendarPacienteServicio(CitaRepository repo,PacienteClient pacienteClient,EspecialistaClient especialistaClient) {
        this.repo = repo;
        this.pacienteClient = pacienteClient;
        this.especialistaClient = especialistaClient;
    }

    public Cita agendar(AgendarPacienteRequest req) {

        if(repo.existsByEspecialistaIdAndFechaAndHora(
                req.getEspecialistaId(),
                req.getFecha(),
                req.getHora()
        )) {
            throw new RuntimeException("Horario ocupado");
        }

        pacienteClient.obtenerPaciente(req.getPacienteId());
        especialistaClient.obtenerEspecialista(req.getEspecialistaId());

        Cita cita = new Cita(
                UUID.randomUUID().toString(),
                req.getPacienteId(),
                "Paciente",
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
