
package co.unicauca.appointment_service.service;

import co.unicauca.appointment_service.repository.CitaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author Juan Martin
 */
@Service
public class HorarioSugeridoServicio {
    private final CitaRepository repo;

    public HorarioSugeridoServicio(CitaRepository repo) {
        this.repo = repo;
    }

    public List<LocalTime> obtener(String especialistaId, LocalDate fecha) {
        return List.of(
                LocalTime.of(8,0),
                LocalTime.of(9,0),
                LocalTime.of(10,0),
                LocalTime.of(11,0)
        ).stream()
                .filter(h -> !repo.existsByEspecialistaIdAndFechaAndHora(
                        especialistaId, fecha, h))
                .toList();
    }
}
