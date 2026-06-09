package co.unicauca.appointment_service.service;

import co.unicauca.appointment_service.client.EspecialistaClient;
import co.unicauca.appointment_service.client.PacienteClient;
import co.unicauca.appointment_service.dto.AgendarPacienteRequest;
import co.unicauca.appointment_service.model.Cita;
import co.unicauca.appointment_service.model.EstadoCita;
import co.unicauca.appointment_service.model.TipoGenero;
import co.unicauca.appointment_service.repository.CitaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 *
 * @author Juan Martin
 */
@Service
public class AgendarPacienteServicio {
    private static final ZoneId ZONA_NEGOCIO = ZoneId.of("America/Bogota");

    private final CitaRepository repo;
    private final PacienteClient pacienteClient;
    private final EspecialistaClient especialistaClient;
    private final HorarioSugeridoServicio horarioServicio;
    private static final Set<EstadoCita> ESTADOS_BLOQUEAN_NUEVA_CITA = EnumSet.of(
            EstadoCita.AGENDADA,
            EstadoCita.PENDIENTE,
            EstadoCita.REAGENDADA
    );

    public AgendarPacienteServicio(CitaRepository repo, PacienteClient pacienteClient, EspecialistaClient especialistaClient, HorarioSugeridoServicio horarioServicio) {
        this.repo = repo;
        this.pacienteClient = pacienteClient;
        this.especialistaClient = especialistaClient;
        this.horarioServicio = horarioServicio;
    }

    public Cita agendar(AgendarPacienteRequest req) {
        validarFecha(req.getFecha());
        validarPacienteSinCitaActiva(req.getPacienteId());

        Map<String, Object> paciente = pacienteClient.obtenerPaciente(req.getPacienteId());
        Map<String, Object> especialista = especialistaClient.obtenerEspecialista(req.getEspecialistaId());
        String especialidad = valorComoTexto(especialista, "especialidad", "");

        validarHorarioDisponible(req.getEspecialistaId(), req.getFecha(), req.getHora());

        Cita cita = new Cita();
        cita.setId(UUID.randomUUID().toString());
        cita.setPacienteId(req.getPacienteId());
        cita.setPacienteNombre(valorComoTexto(paciente, "nombre", "Paciente"));
        cita.setPacienteApellido(valorComoTexto(paciente, "apellido", ""));
        cita.setPacienteTelefono(valorComoTexto(paciente, "telefono", ""));
        cita.setPacienteFechaNacimiento(fechaComoLocalDate(paciente, "fechaNacimiento"));
        cita.setPacienteCorreo(valorComoTexto(paciente, "correo", ""));
        cita.setPacienteGenero(generoComoEnum(paciente, "genero"));
        cita.setEspecialistaId(req.getEspecialistaId());
        cita.setEspecialistaNombre(valorComoTexto(especialista, "nombre", "Especialista"));
        cita.setEspecialistaEspecialidad(especialidad);
        cita.setFecha(req.getFecha());
        cita.setHora(req.getHora());
        cita.setDuracion(60);
        cita.setEstado(EstadoCita.AGENDADA);

        return repo.save(cita);
    }

    private void validarFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new RuntimeException("La fecha es obligatoria");
        }

        if (!fecha.isAfter(LocalDate.now(ZONA_NEGOCIO))) {
            throw new RuntimeException("No se pueden agendar citas para el mismo dia ni en fechas pasadas");
        }
    }

    private void validarPacienteSinCitaActiva(int pacienteId) {
        boolean tieneCitaActiva = repo.findByPacienteId(pacienteId).stream()
                .anyMatch(cita -> ESTADOS_BLOQUEAN_NUEVA_CITA.contains(cita.getEstado()));

        if (tieneCitaActiva) {
            throw new RuntimeException("El paciente ya tiene una cita agendada o pendiente");
        }
    }

    private void validarHorarioDisponible(String especialistaId, LocalDate fecha, LocalTime hora) {
        if (hora == null) {
            throw new RuntimeException("La hora es obligatoria");
        }

        boolean disponible = horarioServicio.obtener(especialistaId, fecha).stream()
                .anyMatch(horario -> horario.equals(hora));

        if (!disponible) {
            throw new RuntimeException("El horario seleccionado no esta disponible para el especialista");
        }
    }

    private String valorComoTexto(Map<String, Object> map, String key, String fallback) {
        if (map == null || map.get(key) == null) return fallback;
        return String.valueOf(map.get(key));
    }

    private LocalDate fechaComoLocalDate(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null || String.valueOf(map.get(key)).isBlank()) {
            return null;
        }

        return LocalDate.parse(String.valueOf(map.get(key)));
    }

    private TipoGenero generoComoEnum(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null || String.valueOf(map.get(key)).isBlank()) {
            return null;
        }

        return TipoGenero.valueOf(String.valueOf(map.get(key)).toUpperCase());
    }
}
