package co.unicauca.appointment_service.validation;

import co.unicauca.appointment_service.model.EstadoCita;
import co.unicauca.appointment_service.repository.CitaRepository;

/**
 * Eslabon de la cadena: rechaza el agendamiento si el horario ya esta ocupado.
 *
 * Conserva exactamente la misma consulta y mensaje que la validacion original.
 *
 * @author Juan Martin
 */
public class ValidadorHorarioOcupado extends ValidadorCita {

    private final CitaRepository repo;

    public ValidadorHorarioOcupado(CitaRepository repo) {
        this.repo = repo;
    }

    @Override
    protected void validarEslabon(ContextoValidacion contexto) {
        boolean ocupado = repo.existsByEspecialistaIdAndFechaAndHoraAndEstadoNot(
                contexto.getEspecialistaId(),
                contexto.getFecha(),
                contexto.getHora(),
                EstadoCita.CANCELADA
        );
        if (ocupado) {
            throw new RuntimeException("Horario ocupado");
        }
    }
}
