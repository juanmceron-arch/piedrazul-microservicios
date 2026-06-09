package co.unicauca.appointment_service.application.usecase;

import co.unicauca.appointment_service.domain.model.Cita;
import co.unicauca.appointment_service.domain.model.EstadoCita;
import co.unicauca.appointment_service.domain.port.in.ReagendarCitaUseCase;
import co.unicauca.appointment_service.domain.port.in.ConsultarHorariosUseCase;
import co.unicauca.appointment_service.domain.port.out.CitaRepositoryPort;
import java.util.UUID;

public class ReagendarCitaService implements ReagendarCitaUseCase {

    private final CitaRepositoryPort repo;
    private final ConsultarHorariosUseCase horariosUseCase;

    public ReagendarCitaService(CitaRepositoryPort repo, ConsultarHorariosUseCase horariosUseCase) {
        this.repo            = repo;
        this.horariosUseCase = horariosUseCase;
    }

    @Override
    public Cita reagendar(String citaId, Comando cmd) {
        var original = repo.buscarPorId(citaId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada: " + citaId));

        // Validar que no haya otra cita activa del paciente
        boolean tieneCitaActiva = repo.buscarPorPaciente(original.getPacienteId()).stream()
                .filter(c -> !c.getId().equals(citaId))
                .anyMatch(Cita::bloqueaNuevaCita);
        if (tieneCitaActiva)
            throw new IllegalStateException("El paciente ya tiene una cita agendada o pendiente");

        // Validar horario disponible
        boolean disponible = horariosUseCase.obtenerDisponibles(original.getEspecialistaId(), cmd.fecha())
                .stream().anyMatch(h -> h.equals(cmd.hora()));
        if (!disponible)
            throw new IllegalStateException("El horario seleccionado no está disponible para el especialista");

        // La regla de dominio valida el estado (solo ASISTIDA puede reagendarse)
        original.reagendar(cmd.fecha(), cmd.hora());

        // Creamos nueva cita AGENDADA (la original queda REAGENDADA)
        Cita nueva = copiar(original);
        nueva.setId(UUID.randomUUID().toString());
        nueva.setFecha(cmd.fecha());
        nueva.setHora(cmd.hora());
        nueva.setEstado(EstadoCita.AGENDADA);

        repo.guardar(original);  // persiste estado REAGENDADA
        return repo.guardar(nueva);
    }

    private Cita copiar(Cita src) {
        Cita c = new Cita();
        c.setPacienteId(src.getPacienteId());
        c.setPacienteNombre(src.getPacienteNombre());
        c.setPacienteApellido(src.getPacienteApellido());
        c.setPacienteTelefono(src.getPacienteTelefono());
        c.setPacienteFechaNacimiento(src.getPacienteFechaNacimiento());
        c.setPacienteCorreo(src.getPacienteCorreo());
        c.setPacienteGenero(src.getPacienteGenero());
        c.setEspecialistaId(src.getEspecialistaId());
        c.setEspecialistaNombre(src.getEspecialistaNombre());
        c.setEspecialistaEspecialidad(src.getEspecialistaEspecialidad());
        c.setDuracion(src.getDuracion() == null ? 60 : src.getDuracion());
        return c;
    }
}
