package co.unicauca.appointment_service.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * Entidad de dominio pura — sin anotaciones de frameworks.
 */
public class Cita {

    private String id;
    private int pacienteId;
    private String pacienteNombre;
    private String pacienteApellido;
    private String pacienteTelefono;
    private LocalDate pacienteFechaNacimiento;
    private String pacienteCorreo;
    private TipoGenero pacienteGenero;
    private String especialistaId;
    private String especialistaNombre;
    private String especialistaEspecialidad;
    private LocalDate fecha;
    private LocalTime hora;
    private Integer duracion;
    private EstadoCita estado;

    // ── Reglas de dominio ──────────────────────────────────────────────────

    private static final Set<EstadoCita> ESTADOS_BLOQUEAN_NUEVA_CITA =
            EnumSet.of(EstadoCita.AGENDADA, EstadoCita.PENDIENTE, EstadoCita.REAGENDADA);

    /** Cancela la cita validando que el estado lo permita. */
    public void cancelar() {
        if (this.estado == EstadoCita.ASISTIDA || this.estado == EstadoCita.NO_ASISTIDA) {
            throw new IllegalStateException("No se puede cancelar una cita asistida o no asistida");
        }
        this.estado = EstadoCita.CANCELADA;
    }

    /**
     * Reagenda la cita: sólo aplica si ya fue asistida,
     * la nueva fecha es futura y el horario está disponible.
     */
    public void reagendar(LocalDate nuevaFecha, LocalTime nuevaHora) {
        if (this.estado != EstadoCita.ASISTIDA) {
            throw new IllegalStateException("Solo se puede reagendar una cita asistida");
        }
        if (nuevaFecha == null || !nuevaFecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La nueva fecha debe ser futura");
        }
        this.fecha  = nuevaFecha;
        this.hora   = nuevaHora;
        this.estado = EstadoCita.REAGENDADA;
    }

    /** Marca asistencia; solo válido después de la fecha de la cita. */
    public void marcarAsistencia(EstadoCita nuevoEstado) {
        Set<EstadoCita> permitidos = EnumSet.of(EstadoCita.ASISTIDA, EstadoCita.NO_ASISTIDA);
        if (!permitidos.contains(nuevoEstado)) {
            throw new IllegalArgumentException("Solo se permite ASISTIDA o NO_ASISTIDA");
        }
        if (!this.fecha.isBefore(LocalDate.now())) {
            throw new IllegalStateException("Solo se puede marcar asistencia después de la fecha de la cita");
        }
        if (this.estado == EstadoCita.CANCELADA) {
            throw new IllegalStateException("No se puede marcar asistencia en una cita cancelada");
        }
        this.estado = nuevoEstado;
    }

    /** ¿El estado bloquea agendar una nueva cita al mismo paciente? */
    public boolean bloqueaNuevaCita() {
        return ESTADOS_BLOQUEAN_NUEVA_CITA.contains(this.estado);
    }

    // ── Getters / Setters ──────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getPacienteId() { return pacienteId; }
    public void setPacienteId(int pacienteId) { this.pacienteId = pacienteId; }
    public String getPacienteNombre() { return pacienteNombre; }
    public void setPacienteNombre(String pacienteNombre) { this.pacienteNombre = pacienteNombre; }
    public String getPacienteApellido() { return pacienteApellido; }
    public void setPacienteApellido(String pacienteApellido) { this.pacienteApellido = pacienteApellido; }
    public String getPacienteTelefono() { return pacienteTelefono; }
    public void setPacienteTelefono(String pacienteTelefono) { this.pacienteTelefono = pacienteTelefono; }
    public LocalDate getPacienteFechaNacimiento() { return pacienteFechaNacimiento; }
    public void setPacienteFechaNacimiento(LocalDate pacienteFechaNacimiento) { this.pacienteFechaNacimiento = pacienteFechaNacimiento; }
    public String getPacienteCorreo() { return pacienteCorreo; }
    public void setPacienteCorreo(String pacienteCorreo) { this.pacienteCorreo = pacienteCorreo; }
    public TipoGenero getPacienteGenero() { return pacienteGenero; }
    public void setPacienteGenero(TipoGenero pacienteGenero) { this.pacienteGenero = pacienteGenero; }
    public String getEspecialistaId() { return especialistaId; }
    public void setEspecialistaId(String especialistaId) { this.especialistaId = especialistaId; }
    public String getEspecialistaNombre() { return especialistaNombre; }
    public void setEspecialistaNombre(String especialistaNombre) { this.especialistaNombre = especialistaNombre; }
    public String getEspecialistaEspecialidad() { return especialistaEspecialidad; }
    public void setEspecialistaEspecialidad(String especialistaEspecialidad) { this.especialistaEspecialidad = especialistaEspecialidad; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public Integer getDuracion() { return duracion; }
    public void setDuracion(Integer duracion) { this.duracion = duracion; }
    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }
}
