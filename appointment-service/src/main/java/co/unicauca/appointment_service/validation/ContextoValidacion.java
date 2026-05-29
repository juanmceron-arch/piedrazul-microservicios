package co.unicauca.appointment_service.validation;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Objeto de contexto que viaja por la cadena de validacion
 * (patron Chain of Responsibility).
 *
 * @author Juan Martin
 */
public class ContextoValidacion {

    private final String especialistaId;
    private final LocalDate fecha;
    private final LocalTime hora;

    public ContextoValidacion(String especialistaId, LocalDate fecha, LocalTime hora) {
        this.especialistaId = especialistaId;
        this.fecha = fecha;
        this.hora = hora;
    }

    public String getEspecialistaId() {
        return especialistaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }
}
