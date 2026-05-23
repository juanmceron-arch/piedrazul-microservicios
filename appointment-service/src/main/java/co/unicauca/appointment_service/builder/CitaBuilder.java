package co.unicauca.appointment_service.builder;

import co.unicauca.appointment_service.model.Cita;
import co.unicauca.appointment_service.model.EstadoCita;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Define los pasos comunes para construir una cita sin exponer constructores extensos.
 */
public interface CitaBuilder<T extends CitaBuilder<T>> {

    T conId(String id);

    T conPaciente(int pacienteId, String pacienteNombre);

    T conEspecialista(String especialistaId, String especialistaNombre);

    T conFecha(LocalDate fecha);

    T conHora(LocalTime hora);

    T conDuracion(Integer duracion);

    T conEstado(EstadoCita estado);

    Cita build();
}
