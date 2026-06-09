package co.unicauca.appointment_service.service;

import co.unicauca.appointment_service.model.Cita;
import co.unicauca.appointment_service.model.EstadoCita;
import co.unicauca.appointment_service.repository.CitaRepository;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class CambiarEstadoCitaServicio {
    private static final Set<EstadoCita> ESTADOS_ASISTENCIA = EnumSet.of(
            EstadoCita.ASISTIDA,
            EstadoCita.NO_ASISTIDA
    );

    private final CitaRepository repo;

    public CambiarEstadoCitaServicio(CitaRepository repo) {
        this.repo = repo;
    }

    public Cita cambiar(String id, EstadoCita estado) {
        if (!ESTADOS_ASISTENCIA.contains(estado)) {
            throw new RuntimeException("Solo se permite cambiar el estado a ASISTIDA o NO_ASISTIDA");
        }

        Cita cita = repo.findById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (!cita.getFecha().isBefore(LocalDate.now())) {
            throw new RuntimeException("Solo se puede marcar asistencia despues de la fecha de la cita");
        }

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new RuntimeException("No se puede marcar asistencia en una cita cancelada");
        }

        cita.setEstado(estado);
        return repo.save(cita);
    }
}
