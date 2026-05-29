package co.unicauca.appointment_service.service;

import co.unicauca.appointment_service.client.EspecialistaGateway;
import co.unicauca.appointment_service.dto.AgendarAgendadorRequest;
import co.unicauca.appointment_service.model.Cita;
import co.unicauca.appointment_service.repository.CitaRepository;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Subclase concreta del Template Method para el agendamiento manual
 * realizado por el agendador.
 *
 * @author Juan Martin
 */
@Service
public class AgendarAgendadorServicio extends AgendarCitaTemplate {

    private final EspecialistaGateway especialistaClient;

    public AgendarAgendadorServicio(CitaRepository repo, EspecialistaGateway especialistaClient) {
        super(repo);
        this.especialistaClient = especialistaClient;
    }

    public Cita agendar(AgendarAgendadorRequest req) {
        return agendarCita(
                req.getPacienteId(),
                req.getNombrePaciente(),
                req.getApellidoPaciente(),
                req.getEspecialistaId(),
                req.getFecha(),
                req.getHora()
        );
    }

    @Override
    protected String resolverNombrePaciente(int pacienteId, String nombre, String apellido) {
        return (String.valueOf(nombre == null ? "" : nombre) + " "
                + String.valueOf(apellido == null ? "" : apellido)).trim();
    }

    @Override
    protected String resolverNombreEspecialista(String especialistaId) {
        Map<String, Object> especialista = especialistaClient.obtenerEspecialista(especialistaId);
        return valorComoTexto(especialista, "nombre", "Especialista");
    }
}
