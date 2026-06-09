package co.unicauca.appointment_service.infrastructure.adapter.in.rest;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;

/** DTOs de la capa REST (adaptador de entrada). */
public class CitaRequest {

    @Data
    public static class AgendarPaciente {
        private int pacienteId;
        private String especialistaId;
        private LocalDate fecha;
        private LocalTime hora;
    }

    @Data
    public static class AgendarAgendador {
        private int pacienteId;
        private String nombrePaciente;
        private String apellidoPaciente;
        private String telefono;
        private LocalDate fechaNacimiento;
        private String correo;
        private String genero;
        private String especialistaId;
        private LocalDate fecha;
        private LocalTime hora;
    }

    @Data
    public static class Reagendar {
        private LocalDate fecha;
        private LocalTime hora;
    }
}
