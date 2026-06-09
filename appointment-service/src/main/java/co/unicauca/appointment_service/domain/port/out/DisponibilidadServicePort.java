package co.unicauca.appointment_service.domain.port.out;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/** Puerto de salida: obtener disponibilidad de un especialista. */
public interface DisponibilidadServicePort {

    record Disponibilidad(
            List<DayOfWeek> diasAtencion,
            LocalTime horaInicio,
            LocalTime horaFin,
            int intervaloMinutos,
            int semanasHabilitadas) {}

    Disponibilidad obtener(String especialistaId);
}
