package co.unicauca.appointment_service.service;

import co.unicauca.appointment_service.client.EspecialistaGateway;
import co.unicauca.appointment_service.client.PacienteClient;
import co.unicauca.appointment_service.dto.AgendarPacienteRequest;
import co.unicauca.appointment_service.model.Cita;
import co.unicauca.appointment_service.repository.CitaRepository;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Subclase concreta del Template Method para el agendamiento que el
 * propio paciente realiza desde el portal web.
 *
 * @author Juan Martin
 */
@Service
public class AgendarPacienteServicio extends AgendarCitaTemplate {

    private final PacienteClient pacienteClient;
    private final EspecialistaGateway especialistaClient;

    public AgendarPacienteServicio(CitaRepository repo, PacienteClient pacienteClient, EspecialistaGateway especialistaClient) {
        super(repo);
        this.pacienteClient = pacienteClient;
        this.especialistaClient = especialistaClient;
    }

    public Cita agendar(AgendarPacienteRequest req) {
        return agendarCita(
                req.getPacienteId(),
                null,
                null,
                req.getEspecialistaId(),
                req.getFecha(),
                req.getHora()
        );
    }

    @Override
    protected String resolverNombrePaciente(int pacienteId, String nombre, String apellido) {
        Map<String, Object> paciente = pacienteClient.obtenerPaciente(pacienteId);
        String nombrePaciente = valorComoTexto(paciente, "nombre", "Paciente");
        String apellidoPaciente = valorComoTexto(paciente, "apellido", "");
        return (nombrePaciente + " " + apellidoPaciente).trim();
    }

    @Override
    protected String resolverNombreEspecialista(String especialistaId) {
        Map<String, Object> especialista = especialistaClient.obtenerEspecialista(especialistaId);
        return valorComoTexto(especialista, "nombre", "Especialista");
    }
}
