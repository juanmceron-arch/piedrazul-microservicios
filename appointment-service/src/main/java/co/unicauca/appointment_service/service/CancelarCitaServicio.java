package co.unicauca.appointment_service.service;

import co.unicauca.appointment_service.repository.CitaRepository;
import org.springframework.stereotype.Service;

/**
 *
 * @author Juan Martin
 */
@Service
public class CancelarCitaServicio {
    private final CitaRepository repo;

    public CancelarCitaServicio(CitaRepository repo) {
        this.repo = repo;
    }

    public String cancelar(String id) {
        var cita = repo.findById(id).orElseThrow();
        cita.cancelar();
        repo.save(cita);
        
        return "Cita cancelada exitosamente";
    }
}
