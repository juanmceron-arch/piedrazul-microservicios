package co.unicauca.appointment_service.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;

/**
 *
 * @author Juan Martin
 */
@Data
public class AgendarPacienteRequest {
    private int pacienteId;
    private String especialistaId;
    private LocalDate fecha;
    private LocalTime hora;
}
