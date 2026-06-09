package co.unicauca.appointment_service.domain.port.in;

import co.unicauca.appointment_service.domain.model.Cita;
import java.time.LocalDate;
import java.time.LocalTime;

/** Puerto de entrada: reagendar una cita. */
public interface ReagendarCitaUseCase {

    record Comando(LocalDate fecha, LocalTime hora) {}

    Cita reagendar(String citaId, Comando cmd);
}
