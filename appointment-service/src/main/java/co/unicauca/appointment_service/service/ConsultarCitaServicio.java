package co.unicauca.appointment_service.service;

import co.unicauca.appointment_service.model.Cita;
import co.unicauca.appointment_service.repository.CitaRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author Juan Martin
 */
@Service
public class ConsultarCitaServicio {
    private final CitaRepository repo;

    public ConsultarCitaServicio(CitaRepository repo) {
        this.repo = repo;
    }

    public List<Cita> listar() {
        return repo.findAll();
    }
}
