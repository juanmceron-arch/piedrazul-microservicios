package co.unicauca.appointment_service.domain.port.in;

import co.unicauca.appointment_service.domain.model.Cita;
import java.time.LocalDate;
import java.time.LocalTime;

/** Puerto de entrada: agendar una cita (paciente o agendador). */
public interface AgendarCitaUseCase {

    record ComandoPaciente(
            int pacienteId,
            String especialistaId,
            LocalDate fecha,
            LocalTime hora) {}

    record ComandoAgendador(
            int pacienteId,
            String nombrePaciente,
            String apellidoPaciente,
            String telefono,
            LocalDate fechaNacimiento,
            String correo,
            String genero,
            String especialistaId,
            LocalDate fecha,
            LocalTime hora) {}

    Cita agendarComoPaciente(ComandoPaciente cmd);

    Cita agendarComoAgendador(ComandoAgendador cmd);
}
