package co.unicauca.appointment_service.service;

import co.unicauca.appointment_service.client.DisponibilidadClient;
import co.unicauca.appointment_service.model.EstadoCita;
import co.unicauca.appointment_service.repository.CitaRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 *
 * @author Juan Martin
 */
@Service
public class HorarioSugeridoServicio {
    private final CitaRepository repo;
    private final DisponibilidadClient disponibilidadClient;

    public HorarioSugeridoServicio(CitaRepository repo, DisponibilidadClient disponibilidadClient) {
        this.repo = repo;
        this.disponibilidadClient = disponibilidadClient;
    }

    public List<LocalTime> obtener(String especialistaId, LocalDate fecha) {
        if (fecha == null || fecha.isBefore(LocalDate.now())) {
            return List.of();
        }

        Map<String, Object> disponibilidad = disponibilidadClient.obtenerDisponibilidad(especialistaId);
        if (disponibilidad == null || disponibilidad.isEmpty()) {
            return List.of();
        }

        int semanasHabilitadas = numero(disponibilidad.get("semanasHabilitadas"), 0);
        if (semanasHabilitadas > 0 && fecha.isAfter(LocalDate.now().plusWeeks(semanasHabilitadas))) {
            return List.of();
        }

        if (!atiendeEseDia(disponibilidad.get("diasAtencion"), fecha.getDayOfWeek())) {
            return List.of();
        }

        LocalTime inicio = LocalTime.parse(String.valueOf(disponibilidad.get("horaInicio")));
        LocalTime fin = LocalTime.parse(String.valueOf(disponibilidad.get("horaFin")));
        int intervalo = numero(disponibilidad.get("intervaloMinutos"), 60);

        List<LocalTime> horarios = new ArrayList<>();
        LocalTime actual = inicio;
        while (actual.plusMinutes(intervalo).compareTo(fin) <= 0) {
            boolean ocupado = repo.existsByEspecialistaIdAndFechaAndHoraAndEstadoNot(
                    especialistaId, fecha, actual, EstadoCita.CANCELADA
            );
            if (!ocupado) {
                horarios.add(actual);
            }
            actual = actual.plusMinutes(intervalo);
        }

        return horarios;
    }

    private boolean atiendeEseDia(Object diasAtencion, DayOfWeek dia) {
        if (!(diasAtencion instanceof List<?> dias)) return false;
        return dias.stream().map(String::valueOf).anyMatch(d -> d.equalsIgnoreCase(dia.name()));
    }

    private int numero(Object valor, int fallback) {
        if (valor instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(valor)); } catch (Exception e) { return fallback; }
    }
}
