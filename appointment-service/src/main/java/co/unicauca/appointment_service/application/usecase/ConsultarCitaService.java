package co.unicauca.appointment_service.application.usecase;

import co.unicauca.appointment_service.domain.model.Cita;
import co.unicauca.appointment_service.domain.port.in.ConsultarCitaUseCase;
import co.unicauca.appointment_service.domain.port.out.CitaRepositoryPort;
import java.util.List;

public class ConsultarCitaService implements ConsultarCitaUseCase {

    private final CitaRepositoryPort repo;

    public ConsultarCitaService(CitaRepositoryPort repo) {
        this.repo = repo;
    }

    @Override
    public List<Cita> listar() {
        return repo.listarTodas();
    }
}
