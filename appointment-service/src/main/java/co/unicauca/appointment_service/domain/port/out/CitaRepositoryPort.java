package co.unicauca.appointment_service.domain.port.out;

import co.unicauca.appointment_service.domain.model.Cita;
import co.unicauca.appointment_service.domain.model.EstadoCita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/** Puerto de salida: persistencia de citas (adaptador: JPA/SQLite). */
public interface CitaRepositoryPort {
    Cita guardar(Cita cita);
    Optional<Cita> buscarPorId(String id);
    List<Cita> listarTodas();
    List<Cita> buscarPorPaciente(int pacienteId);
    boolean existeOcupado(String especialistaId, LocalDate fecha, LocalTime hora, EstadoCita estadoExcluido);
}
