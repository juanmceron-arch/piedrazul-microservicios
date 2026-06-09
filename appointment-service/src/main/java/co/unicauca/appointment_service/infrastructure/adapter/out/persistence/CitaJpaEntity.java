package co.unicauca.appointment_service.infrastructure.adapter.out.persistence;

import co.unicauca.appointment_service.domain.model.EstadoCita;
import co.unicauca.appointment_service.domain.model.TipoGenero;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

/** Entidad JPA: vive solo en la capa de infraestructura. */
@Entity
@Table(name = "cita")
public class CitaJpaEntity {

    @Id
    private String id;
    private int pacienteId;
    private String pacienteNombre;
    private String pacienteApellido;
    private String pacienteTelefono;
    private LocalDate pacienteFechaNacimiento;
    private String pacienteCorreo;

    @Enumerated(EnumType.STRING)
    private TipoGenero pacienteGenero;

    private String especialistaId;
    private String especialistaNombre;
    private String especialistaEspecialidad;
    private LocalDate fecha;
    private LocalTime hora;
    private Integer duracion;

    @Enumerated(EnumType.STRING)
    private EstadoCita estado;

    // Getters y Setters
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
    public void setPacienteFechaNacimiento(LocalDate v) { this.pacienteFechaNacimiento = v; }
    public String getPacienteCorreo() { return pacienteCorreo; }
    public void setPacienteCorreo(String pacienteCorreo) { this.pacienteCorreo = pacienteCorreo; }
    public TipoGenero getPacienteGenero() { return pacienteGenero; }
    public void setPacienteGenero(TipoGenero pacienteGenero) { this.pacienteGenero = pacienteGenero; }
    public String getEspecialistaId() { return especialistaId; }
    public void setEspecialistaId(String especialistaId) { this.especialistaId = especialistaId; }
    public String getEspecialistaNombre() { return especialistaNombre; }
    public void setEspecialistaNombre(String especialistaNombre) { this.especialistaNombre = especialistaNombre; }
    public String getEspecialistaEspecialidad() { return especialistaEspecialidad; }
    public void setEspecialistaEspecialidad(String v) { this.especialistaEspecialidad = v; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public Integer getDuracion() { return duracion; }
    public void setDuracion(Integer duracion) { this.duracion = duracion; }
    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }
}
