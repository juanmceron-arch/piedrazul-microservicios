package co.unicauca.appointment_service.builder;

import co.unicauca.appointment_service.model.Cita;
import co.unicauca.appointment_service.model.EstadoCita;
import java.util.UUID;

/**
 * Builder concreto para citas nuevas en estado AGENDADA.
 */
public class CitaAgendadaBuilder extends AbstractCitaBuilder<CitaAgendadaBuilder> {

    private static final int DURACION_POR_DEFECTO = 60;

    public CitaAgendadaBuilder() {
        this.id = UUID.randomUUID().toString();
        this.duracion = DURACION_POR_DEFECTO;
        this.estado = EstadoCita.AGENDADA;
    }

    @Override
    protected CitaAgendadaBuilder self() {
        return this;
    }

    @Override
    public Cita build() {
        validarCamposObligatorios();
        return crearCita();
    }
}
