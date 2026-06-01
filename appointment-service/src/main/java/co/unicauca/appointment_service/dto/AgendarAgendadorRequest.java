package co.unicauca.appointment_service.dto;

import co.unicauca.appointment_service.model.TipoGenero;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;

/**
 *
 * @author Juan Martin
 */
@Data
public class AgendarAgendadorRequest {
    
    private int pacienteId;
    private String nombrePaciente;
    private String apellidoPaciente;
    private String telefono;
    private LocalDate fechaNacimiento;
    private String correo;
    
    @Enumerated(EnumType.STRING)
    private TipoGenero genero;

    private String especialistaId;
    
    private LocalDate fecha;
    private LocalTime hora;
}
