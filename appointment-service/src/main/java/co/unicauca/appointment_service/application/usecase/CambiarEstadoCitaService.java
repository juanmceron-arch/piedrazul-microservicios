package co.unicauca.appointment_service.application.usecase;

import co.unicauca.appointment_service.domain.model.Cita;
import co.unicauca.appointment_service.domain.model.EstadoCita;
import co.unicauca.appointment_service.domain.port.in.CambiarEstadoCitaUseCase;
import co.unicauca.appointment_service.domain.port.out.CitaRepositoryPort;

public class CambiarEstadoCitaService implements CambiarEstadoCitaUseCase {

    private final CitaRepositoryPort repo;

    public CambiarEstadoCitaService(CitaRepositoryPort repo) {
        this.repo = repo;
    }

    @Override
    public Cita cambiar(String citaId, EstadoCita estado) {
        Cita cita = repo.buscarPorId(citaId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada: " + citaId));
        cita.marcarAsistencia(estado);   // regla de dominio
        return repo.guardar(cita);
    }
}
