package co.unicauca.appointment_service.domain.port.out;

import java.time.LocalDate;

/** Puerto de salida: obtener datos del servicio de pacientes. */
public interface PacienteServicePort {

    record DatosPaciente(
            int id,
            String nombre,
            String apellido,
            String telefono,
            LocalDate fechaNacimiento,
            String correo,
            String genero) {}

    DatosPaciente obtener(int pacienteId);
}
