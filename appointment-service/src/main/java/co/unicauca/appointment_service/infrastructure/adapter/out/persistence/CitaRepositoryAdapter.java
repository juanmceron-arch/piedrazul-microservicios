package co.unicauca.appointment_service.infrastructure.adapter.out.persistence;

import co.unicauca.appointment_service.domain.model.Cita;
import co.unicauca.appointment_service.domain.model.EstadoCita;
import co.unicauca.appointment_service.domain.port.out.CitaRepositoryPort;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida: implementa CitaRepositoryPort usando JPA/SQLite.
 * Mapea entre la entidad de dominio (Cita) y la entidad JPA (CitaJpaEntity).
 */
@Component
public class CitaRepositoryAdapter implements CitaRepositoryPort {

    private final CitaJpaRepository jpa;

    public CitaRepositoryAdapter(CitaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Cita guardar(Cita cita) {
        return toDomain(jpa.save(toEntity(cita)));
    }

    @Override
    public Optional<Cita> buscarPorId(String id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Cita> listarTodas() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Cita> buscarPorPaciente(int pacienteId) {
        return jpa.findByPacienteId(pacienteId).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existeOcupado(String especialistaId, LocalDate fecha, LocalTime hora, EstadoCita estadoExcluido) {
        return jpa.existsByEspecialistaIdAndFechaAndHoraAndEstadoNot(especialistaId, fecha, hora, estadoExcluido);
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private CitaJpaEntity toEntity(Cita d) {
        CitaJpaEntity e = new CitaJpaEntity();
        e.setId(d.getId());
        e.setPacienteId(d.getPacienteId());
        e.setPacienteNombre(d.getPacienteNombre());
        e.setPacienteApellido(d.getPacienteApellido());
        e.setPacienteTelefono(d.getPacienteTelefono());
        e.setPacienteFechaNacimiento(d.getPacienteFechaNacimiento());
        e.setPacienteCorreo(d.getPacienteCorreo());
        e.setPacienteGenero(d.getPacienteGenero());
        e.setEspecialistaId(d.getEspecialistaId());
        e.setEspecialistaNombre(d.getEspecialistaNombre());
        e.setEspecialistaEspecialidad(d.getEspecialistaEspecialidad());
        e.setFecha(d.getFecha());
        e.setHora(d.getHora());
        e.setDuracion(d.getDuracion());
        e.setEstado(d.getEstado());
        return e;
    }

    private Cita toDomain(CitaJpaEntity e) {
        Cita d = new Cita();
        d.setId(e.getId());
        d.setPacienteId(e.getPacienteId());
        d.setPacienteNombre(e.getPacienteNombre());
        d.setPacienteApellido(e.getPacienteApellido());
        d.setPacienteTelefono(e.getPacienteTelefono());
        d.setPacienteFechaNacimiento(e.getPacienteFechaNacimiento());
        d.setPacienteCorreo(e.getPacienteCorreo());
        d.setPacienteGenero(e.getPacienteGenero());
        d.setEspecialistaId(e.getEspecialistaId());
        d.setEspecialistaNombre(e.getEspecialistaNombre());
        d.setEspecialistaEspecialidad(e.getEspecialistaEspecialidad());
        d.setFecha(e.getFecha());
        d.setHora(e.getHora());
        d.setDuracion(e.getDuracion());
        d.setEstado(e.getEstado());
        return d;
    }
}
