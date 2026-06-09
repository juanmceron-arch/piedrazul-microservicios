package co.unicauca.appointment_service.application.usecase;

import co.unicauca.appointment_service.domain.model.Cita;
import co.unicauca.appointment_service.domain.model.EstadoCita;
import co.unicauca.appointment_service.domain.model.TipoGenero;
import co.unicauca.appointment_service.domain.port.in.AgendarCitaUseCase;
import co.unicauca.appointment_service.domain.port.in.ConsultarHorariosUseCase;
import co.unicauca.appointment_service.domain.port.out.CitaRepositoryPort;
import co.unicauca.appointment_service.domain.port.out.EspecialistaServicePort;
import co.unicauca.appointment_service.domain.port.out.PacienteServicePort;
import java.time.LocalDate;
import java.util.UUID;

public class AgendarCitaService implements AgendarCitaUseCase {

    private final CitaRepositoryPort repo;
    private final PacienteServicePort pacientePort;
    private final EspecialistaServicePort especialistaPort;
    private final ConsultarHorariosUseCase horariosUseCase;

    public AgendarCitaService(CitaRepositoryPort repo,
                               PacienteServicePort pacientePort,
                               EspecialistaServicePort especialistaPort,
                               ConsultarHorariosUseCase horariosUseCase) {
        this.repo             = repo;
        this.pacientePort     = pacientePort;
        this.especialistaPort = especialistaPort;
        this.horariosUseCase  = horariosUseCase;
    }

    @Override
    public Cita agendarComoPaciente(ComandoPaciente cmd) {
        validarFecha(cmd.fecha());
        validarSinCitaActiva(cmd.pacienteId());

        PacienteServicePort.DatosPaciente paciente         = pacientePort.obtener(cmd.pacienteId());
        EspecialistaServicePort.DatosEspecialista especialista = especialistaPort.obtener(cmd.especialistaId());

        validarPrimeraCitaMedicinaGeneral(cmd.pacienteId(), especialista.especialidad());
        validarHorarioDisponible(cmd.especialistaId(), cmd.fecha(), cmd.hora());

        Cita cita = buildCita(cmd.pacienteId(), cmd.especialistaId(), cmd.fecha(), cmd.hora());
        cita.setPacienteNombre(paciente.nombre());
        cita.setPacienteApellido(paciente.apellido());
        cita.setPacienteTelefono(paciente.telefono());
        cita.setPacienteFechaNacimiento(paciente.fechaNacimiento());
        cita.setPacienteCorreo(paciente.correo());
        cita.setPacienteGenero(toGenero(paciente.genero()));
        cita.setEspecialistaNombre(especialista.nombre());
        cita.setEspecialistaEspecialidad(especialista.especialidad());
        return repo.guardar(cita);
    }

    @Override
    public Cita agendarComoAgendador(ComandoAgendador cmd) {
        validarFecha(cmd.fecha());
        validarSinCitaActiva(cmd.pacienteId());

        EspecialistaServicePort.DatosEspecialista especialista = especialistaPort.obtener(cmd.especialistaId());
        validarHorarioDisponible(cmd.especialistaId(), cmd.fecha(), cmd.hora());

        Cita cita = buildCita(cmd.pacienteId(), cmd.especialistaId(), cmd.fecha(), cmd.hora());
        cita.setPacienteNombre(cmd.nombrePaciente());
        cita.setPacienteApellido(cmd.apellidoPaciente());
        cita.setPacienteTelefono(cmd.telefono());
        cita.setPacienteFechaNacimiento(cmd.fechaNacimiento());
        cita.setPacienteCorreo(cmd.correo());
        cita.setPacienteGenero(toGenero(cmd.genero()));
        cita.setEspecialistaNombre(especialista.nombre());
        cita.setEspecialistaEspecialidad(especialista.especialidad());
        return repo.guardar(cita);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Cita buildCita(int pacienteId, String especialistaId, LocalDate fecha, java.time.LocalTime hora) {
        Cita c = new Cita();
        c.setId(UUID.randomUUID().toString());
        c.setPacienteId(pacienteId);
        c.setEspecialistaId(especialistaId);
        c.setFecha(fecha);
        c.setHora(hora);
        c.setDuracion(60);
        c.setEstado(EstadoCita.AGENDADA);
        return c;
    }

    private void validarFecha(LocalDate fecha) {
        if (fecha == null) throw new IllegalArgumentException("La fecha es obligatoria");
        if (!fecha.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("No se pueden agendar citas para el mismo día ni en fechas pasadas");
    }

    private void validarSinCitaActiva(int pacienteId) {
        boolean activa = repo.buscarPorPaciente(pacienteId).stream().anyMatch(Cita::bloqueaNuevaCita);
        if (activa) throw new IllegalStateException("El paciente ya tiene una cita agendada o pendiente");
    }

    private void validarHorarioDisponible(String especialistaId, LocalDate fecha, java.time.LocalTime hora) {
        if (hora == null) throw new IllegalArgumentException("La hora es obligatoria");
        boolean disponible = horariosUseCase.obtenerDisponibles(especialistaId, fecha).stream()
                .anyMatch(h -> h.equals(hora));
        if (!disponible)
            throw new IllegalStateException("El horario seleccionado no está disponible para el especialista");
    }

    private void validarPrimeraCitaMedicinaGeneral(int pacienteId, String especialidad) {
        boolean esPrimera = repo.buscarPorPaciente(pacienteId).isEmpty();
        if (esPrimera && !esMedicinaGeneral(especialidad))
            throw new IllegalStateException("La primera cita del paciente debe ser con medicina general");
    }

    private boolean esMedicinaGeneral(String especialidad) {
        String n = String.valueOf(especialidad).trim().toUpperCase();
        return n.equals("CONSULTA_GENERAL") || n.equals("MEDICINA_GENERAL");
    }

    private TipoGenero toGenero(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try { return TipoGenero.valueOf(valor.trim().toUpperCase()); } catch (Exception e) { return null; }
    }
}
