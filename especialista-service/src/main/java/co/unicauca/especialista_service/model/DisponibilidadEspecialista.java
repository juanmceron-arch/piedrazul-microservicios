package co.unicauca.especialista_service.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 *
 * @author Juan Martin
 */
public class DisponibilidadEspecialista {
    private List<DayOfWeek> diasAtencion;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int semanasHabilitadas;
    private int intervaloMinutos;

    public DisponibilidadEspecialista() {
    }

    public List<DayOfWeek> getDiasAtencion() {
        return diasAtencion;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public int getSemanasHabilitadas() {
        return semanasHabilitadas;
    }

    public int getIntervaloMinutos() {
        return intervaloMinutos;
    }

    public void setDiasAtencion(List<DayOfWeek> diasAtencion) {
        this.diasAtencion = diasAtencion;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public void setSemanasHabilitadas(int semanasHabilitadas) {
        this.semanasHabilitadas = semanasHabilitadas;
    }

    public void setIntervaloMinutos(int intervaloMinutos) {
        this.intervaloMinutos = intervaloMinutos;
    }
    
}
