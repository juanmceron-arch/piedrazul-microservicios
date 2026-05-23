package co.unicauca.appointment_service.service;

import co.unicauca.appointment_service.dto.ReagendarRequest;
import co.unicauca.appointment_service.repository.CitaRepository;
import org.springframework.stereotype.Service;

/**
 *
 * @author Juan Martin
 */
@Service
public class ReagendarCitaServicio {
    private final CitaRepository repo;

    public ReagendarCitaServicio(CitaRepository repo) {
        this.repo = repo;
    }

    public void reagendar(String id, ReagendarRequest req) {
        var cita = repo.findById(id).orElseThrow();
        cita.reagendar(req.getFecha(), req.getHora());
        repo.save(cita);
    }
}
