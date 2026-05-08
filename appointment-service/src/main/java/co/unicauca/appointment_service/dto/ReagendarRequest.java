package co.unicauca.appointment_service.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;

/**
 *
 * @author Juan Martin
 */
@Data
public class ReagendarRequest {
    private LocalDate fecha;
    private LocalTime hora;
}
