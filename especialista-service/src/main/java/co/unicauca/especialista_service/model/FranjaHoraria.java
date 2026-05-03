package co.unicauca.especialista_service.model;

import java.time.LocalTime;

/**
 *
 * @author Juan Martin
 */
public class FranjaHoraria {
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private boolean disponible;

    public FranjaHoraria() {
    }

    public FranjaHoraria(LocalTime horaInicio, LocalTime horaFin, boolean disponible) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.disponible = disponible;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
    
    
}
