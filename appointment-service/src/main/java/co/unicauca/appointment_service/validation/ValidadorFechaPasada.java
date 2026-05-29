package co.unicauca.appointment_service.validation;

import java.time.LocalDate;

/**
 * Eslabon de la cadena: rechaza citas en fechas pasadas.
 *
 * Conserva el mismo criterio y mensaje que tenia la validacion original
 * en los servicios de agendamiento.
 *
 * @author Juan Martin
 */
public class ValidadorFechaPasada extends ValidadorCita {

    @Override
    protected void validarEslabon(ContextoValidacion contexto) {
        LocalDate fecha = contexto.getFecha();
        if (fecha != null && fecha.isBefore(LocalDate.now())) {
            throw new RuntimeException("No se pueden agendar citas en fechas pasadas");
        }
    }
}
