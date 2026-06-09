package co.unicauca.appointment_service.application.usecase;

import co.unicauca.appointment_service.domain.model.EstadoCita;
import co.unicauca.appointment_service.domain.port.in.ConsultarHorariosUseCase;
import co.unicauca.appointment_service.domain.port.out.CitaRepositoryPort;
import co.unicauca.appointment_service.domain.port.out.DisponibilidadServicePort;
import co.unicauca.appointment_service.domain.port.out.DisponibilidadServicePort.Disponibilidad;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ConsultarHorariosService implements ConsultarHorariosUseCase {

    private final CitaRepositoryPort repo;
    private final DisponibilidadServicePort disponibilidadPort;

    public ConsultarHorariosService(CitaRepositoryPort repo,
                                     DisponibilidadServicePort disponibilidadPort) {
        this.repo               = repo;
        this.disponibilidadPort = disponibilidadPort;
    }

    @Override
    public List<LocalTime> obtenerDisponibles(String especialistaId, LocalDate fecha) {
        if (fecha == null || !fecha.isAfter(LocalDate.now())) return List.of();

        Disponibilidad d = disponibilidadPort.obtener(especialistaId);
        if (d == null) return List.of();

        if (d.semanasHabilitadas() > 0 && fecha.isAfter(LocalDate.now().plusWeeks(d.semanasHabilitadas())))
            return List.of();

        if (!d.diasAtencion().contains(fecha.getDayOfWeek())) return List.of();

        List<LocalTime> horarios = new ArrayList<>();
        LocalTime actual = d.horaInicio();
        int intervalo = d.intervaloMinutos() > 0 ? d.intervaloMinutos() : 60;

        while (actual.plusMinutes(intervalo).compareTo(d.horaFin()) <= 0) {
            boolean ocupado = repo.existeOcupado(especialistaId, fecha, actual, EstadoCita.CANCELADA);
            if (!ocupado) horarios.add(actual);
            actual = actual.plusMinutes(intervalo);
        }
        return horarios;
    }
}
