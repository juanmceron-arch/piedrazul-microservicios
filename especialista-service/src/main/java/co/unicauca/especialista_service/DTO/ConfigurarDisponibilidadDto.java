package co.unicauca.especialista_service.DTO;

import java.util.List;

/**
 *
 * @author Juan Martin
 */
public class ConfigurarDisponibilidadDto {
    private List<String> diasAtencion;
    private String horaInicio;
    private String horaFin;
    private Integer intervaloMinutos;
    private Integer semanasHabilitadas;

    public ConfigurarDisponibilidadDto() {
    }

    public List<String> getDiasAtencion() {
        return diasAtencion;
    }

    public void setDiasAtencion(List<String> diasAtencion) {
        this.diasAtencion = diasAtencion;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public Integer getIntervaloMinutos() {
        return intervaloMinutos;
    }

    public void setIntervaloMinutos(Integer intervaloMinutos) {
        this.intervaloMinutos = intervaloMinutos;
    }

    public Integer getSemanasHabilitadas() {
        return semanasHabilitadas;
    }

    public void setSemanasHabilitadas(Integer semanasHabilitadas) {
        this.semanasHabilitadas = semanasHabilitadas;
    }
    
}
