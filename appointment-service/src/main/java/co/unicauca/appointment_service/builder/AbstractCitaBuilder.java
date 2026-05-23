package co.unicauca.appointment_service.builder;

import co.unicauca.appointment_service.model.Cita;
import co.unicauca.appointment_service.model.EstadoCita;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Builder base con la asignacion de atributos compartidos por cualquier tipo de cita.
 */
public abstract class AbstractCitaBuilder<T extends AbstractCitaBuilder<T>> implements CitaBuilder<T> {

    protected String id;
    protected int pacienteId;
    protected String pacienteNombre;
    protected String especialistaId;
    protected String especialistaNombre;
    protected LocalDate fecha;
    protected LocalTime hora;
    protected Integer duracion;
    protected EstadoCita estado;

    @Override
    public T conId(String id) {
        this.id = id;
        return self();
    }

    @Override
    public T conPaciente(int pacienteId, String pacienteNombre) {
        this.pacienteId = pacienteId;
        this.pacienteNombre = pacienteNombre;
        return self();
    }

    @Override
    public T conEspecialista(String especialistaId, String especialistaNombre) {
        this.especialistaId = especialistaId;
        this.especialistaNombre = especialistaNombre;
        return self();
    }

    @Override
    public T conFecha(LocalDate fecha) {
        this.fecha = fecha;
        return self();
    }

    @Override
    public T conHora(LocalTime hora) {
        this.hora = hora;
        return self();
    }

    @Override
    public T conDuracion(Integer duracion) {
        this.duracion = duracion;
        return self();
    }

    @Override
    public T conEstado(EstadoCita estado) {
        this.estado = estado;
        return self();
    }

    protected abstract T self();

    protected void validarCamposObligatorios() {
        if (estaVacio(id)) {
            throw new IllegalArgumentException("El id de la cita es obligatorio");
        }
        if (pacienteId <= 0) {
            throw new IllegalArgumentException("El paciente es obligatorio");
        }
        if (estaVacio(especialistaId)) {
            throw new IllegalArgumentException("El especialista es obligatorio");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de la cita es obligatoria");
        }
        if (hora == null) {
            throw new IllegalArgumentException("La hora de la cita es obligatoria");
        }
        if (duracion == null || duracion <= 0) {
            throw new IllegalArgumentException("La duracion de la cita debe ser mayor a cero");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado de la cita es obligatorio");
        }
    }

    protected Cita crearCita() {
        return new Cita(
                id,
                pacienteId,
                pacienteNombre,
                especialistaId,
                especialistaNombre,
                fecha,
                hora,
                duracion,
                estado
        );
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
