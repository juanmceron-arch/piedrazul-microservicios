package co.unicauca.appointment_service.application.usecase;

import co.unicauca.appointment_service.domain.port.in.CancelarCitaUseCase;
import co.unicauca.appointment_service.domain.port.out.CitaRepositoryPort;

public class CancelarCitaService implements CancelarCitaUseCase {

    private final CitaRepositoryPort repo;

    public CancelarCitaService(CitaRepositoryPort repo) {
        this.repo = repo;
    }

    @Override
    public String cancelar(String citaId) {
        var cita = repo.buscarPorId(citaId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada: " + citaId));
        cita.cancelar();          // regla de dominio
        repo.guardar(cita);
        return "Cita cancelada exitosamente";
    }
}
