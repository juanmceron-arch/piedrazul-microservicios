package co.unicauca.appointment_service.domain.port.in;

/** Puerto de entrada: cancelar una cita. */
public interface CancelarCitaUseCase {
    String cancelar(String citaId);
}
