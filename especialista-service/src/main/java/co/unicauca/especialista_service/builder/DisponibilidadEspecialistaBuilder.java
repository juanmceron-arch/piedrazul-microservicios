package co.unicauca.especialista_service.builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import co.unicauca.especialista_service.model.DisponibilidadEspecialista;

/**
 *
 * @author Juan Martin
 */
public class DisponibilidadEspecialistaBuilder {
    private final DisponibilidadEspecialista disponibilidad;

    public DisponibilidadEspecialistaBuilder() {
        disponibilidad = new DisponibilidadEspecialista();
    }
    
    public DisponibilidadEspecialistaBuilder diasAtencion(List<DayOfWeek> dias) {
        disponibilidad.setDiasAtencion(dias);
        return this;
    }

    public DisponibilidadEspecialistaBuilder horaInicio(LocalTime horaInicio) {
        disponibilidad.setHoraInicio(horaInicio);
        return this;
    }

     public DisponibilidadEspecialistaBuilder horaFin(LocalTime horaFin) {
        disponibilidad.setHoraFin(horaFin);
        return this;
    }

    public DisponibilidadEspecialistaBuilder intervaloMinutos(Integer intervalo) {
        disponibilidad.setIntervaloMinutos(intervalo);
        return this;
    }

    public DisponibilidadEspecialistaBuilder semanasHabilitadas(Integer semanas) {
        disponibilidad.setSemanasHabilitadas(semanas);
        return this;
    }
    
    public DisponibilidadEspecialista build() {
        return disponibilidad;
    }    
    
}
