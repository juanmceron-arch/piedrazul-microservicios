package com.piedrazul.citas.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Datos del paciente (antes era un objeto Paciente)
    private String pacienteId;
    private String pacienteNombre;
    private String pacienteApellido;
    private String pacienteTelefono;
    private String pacienteGenero;

    // Datos del especialista (antes era un objeto Especialista)
    private String especialistaId;
    private String especialistaNombre;
    private String especialistaEspecialidad;

    private LocalDate fecha;
    private LocalTime hora;
    private int duracionMinutos;

    @Enumerated(EnumType.STRING)
    private EstadoCita estadoCita;

    public Cita() {}

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getPacienteNombre() { return pacienteNombre; }
    public void setPacienteNombre(String pacienteNombre) { this.pacienteNombre = pacienteNombre; }

    public String getPacienteApellido() { return pacienteApellido; }
    public void setPacienteApellido(String pacienteApellido) { this.pacienteApellido = pacienteApellido; }

    public String getPacienteTelefono() { return pacienteTelefono; }
    public void setPacienteTelefono(String pacienteTelefono) { this.pacienteTelefono = pacienteTelefono; }

    public String getPacienteGenero() { return pacienteGenero; }
    public void setPacienteGenero(String pacienteGenero) { this.pacienteGenero = pacienteGenero; }

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

    public int getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(int duracionMinutos) { this.duracionMinutos = duracionMinutos; }

    public EstadoCita getEstadoCita() { return estadoCita; }
    public void setEstadoCita(EstadoCita estadoCita) { this.estadoCita = estadoCita; }
}