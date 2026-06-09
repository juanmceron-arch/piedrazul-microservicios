package co.unicauca.appointment_service.domain.port.in;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Puerto de entrada: obtener horarios disponibles de un especialista. */
public interface ConsultarHorariosUseCase {
    List<LocalTime> obtenerDisponibles(String especialistaId, LocalDate fecha);
}
