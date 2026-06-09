package co.unicauca.appointment_service.domain.port.in;

import co.unicauca.appointment_service.domain.model.Cita;
import co.unicauca.appointment_service.domain.model.EstadoCita;

/** Puerto de entrada: marcar asistencia de una cita. */
public interface CambiarEstadoCitaUseCase {
    Cita cambiar(String citaId, EstadoCita estado);
}
