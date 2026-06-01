package co.unicauca.appointment_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.*;

/**
 *
 * @author Juan Martin
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cita {
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

    public void cancelar() {
        this.estado = EstadoCita.CANCELADA;
    }

    public void reagendar(LocalDate fecha, LocalTime hora) {
        this.fecha = fecha;
        this.hora = hora;
        this.estado = EstadoCita.REAGENDADA;
    }
}
