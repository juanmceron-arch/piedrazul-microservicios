package co.unicauca.appointment_service.service;

import co.unicauca.appointment_service.dto.ReagendarRequest;
import co.unicauca.appointment_service.model.Cita;
import co.unicauca.appointment_service.model.EstadoCita;
import co.unicauca.appointment_service.repository.CitaRepository;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 *
 * @author Juan Martin
 */
@Service
public class ReagendarCitaServicio {
    private final CitaRepository repo;
    private final HorarioSugeridoServicio horarioServicio;
    private static final Set<EstadoCita> ESTADOS_BLOQUEAN_NUEVA_CITA = EnumSet.of(
            EstadoCita.AGENDADA,
            EstadoCita.PENDIENTE,
            EstadoCita.REAGENDADA
    );

    public ReagendarCitaServicio(CitaRepository repo, HorarioSugeridoServicio horarioServicio) {
        this.repo = repo;
        this.horarioServicio = horarioServicio;
    }

    public Cita reagendar(String id, ReagendarRequest req) {
        var cita = repo.findById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (cita.getEstado() != EstadoCita.ASISTIDA) {
            throw new RuntimeException("Solo se puede reagendar una cita asistida");
        }

        if (req.getFecha() == null || req.getHora() == null) {
            throw new RuntimeException("Fecha y hora son obligatorias");
        }

        if (!req.getFecha().isAfter(LocalDate.now())) {
            throw new RuntimeException("No se pueden reagendar citas para el mismo dia ni en fechas pasadas");
        }

        boolean tieneCitaActiva = repo.findByPacienteId(cita.getPacienteId()).stream()
                .anyMatch(actual -> ESTADOS_BLOQUEAN_NUEVA_CITA.contains(actual.getEstado()));

        if (tieneCitaActiva) {
            throw new RuntimeException("El paciente ya tiene una cita agendada o pendiente");
        }

        boolean disponible = horarioServicio.obtener(cita.getEspecialistaId(), req.getFecha()).stream()
                .anyMatch(horario -> horario.equals(req.getHora()));

        if (!disponible) {
            throw new RuntimeException("El horario seleccionado no esta disponible para el especialista");
        }

        Cita nuevaCita = new Cita();
        nuevaCita.setId(UUID.randomUUID().toString());
        nuevaCita.setPacienteId(cita.getPacienteId());
        nuevaCita.setPacienteNombre(cita.getPacienteNombre());
        nuevaCita.setPacienteApellido(cita.getPacienteApellido());
        nuevaCita.setPacienteTelefono(cita.getPacienteTelefono());
        nuevaCita.setPacienteFechaNacimiento(cita.getPacienteFechaNacimiento());
        nuevaCita.setPacienteCorreo(cita.getPacienteCorreo());
        nuevaCita.setPacienteGenero(cita.getPacienteGenero());
        nuevaCita.setEspecialistaId(cita.getEspecialistaId());
        nuevaCita.setEspecialistaNombre(cita.getEspecialistaNombre());
        nuevaCita.setEspecialistaEspecialidad(cita.getEspecialistaEspecialidad());
        nuevaCita.setFecha(req.getFecha());
        nuevaCita.setHora(req.getHora());
        nuevaCita.setDuracion(cita.getDuracion() == null ? 60 : cita.getDuracion());
        nuevaCita.setEstado(EstadoCita.AGENDADA);

        return repo.save(nuevaCita);
    }
}
