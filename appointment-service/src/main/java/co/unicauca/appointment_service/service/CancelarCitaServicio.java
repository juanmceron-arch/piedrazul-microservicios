package co.unicauca.appointment_service.service;

import co.unicauca.appointment_service.model.EstadoCita;
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
        var cita = repo.findById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (cita.getEstado() == EstadoCita.ASISTIDA || cita.getEstado() == EstadoCita.NO_ASISTIDA) {
            throw new RuntimeException("No se puede cancelar una cita asistida o no asistida");
        }

        cita.cancelar();
        repo.save(cita);
        
        return "Cita cancelada exitosamente";
    }
}
