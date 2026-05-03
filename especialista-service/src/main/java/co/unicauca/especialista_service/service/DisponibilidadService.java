package co.unicauca.especialista_service.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import co.unicauca.especialista_service.model.DisponibilidadEspecialista;
import co.unicauca.especialista_service.model.FranjaHoraria;
import co.unicauca.especialista_service.repository.DisponibilidadRepositorio;
import org.springframework.stereotype.Service;

/**
 *
 * @author Juan Martin
 */
@Service
public class DisponibilidadService {
    
    private final DisponibilidadRepositorio repositorio;

    public DisponibilidadService(DisponibilidadRepositorio repositorio) {
        this.repositorio = repositorio;
    }
    
    public void configurarDisponibilidad(String especialistaId, DisponibilidadEspecialista disponibilidad) {

        if (!validarHorario(disponibilidad)) {
            throw new IllegalArgumentException("Horario inválido");
        }

        repositorio.guardar(especialistaId, disponibilidad);
    }
    
    public DisponibilidadEspecialista consultarDisponibilidad(String especialistaId) {
        return repositorio.buscarPorEspecialistaId(especialistaId);
    }

    public boolean validarHorario(DisponibilidadEspecialista disponibilidad) {

        if (disponibilidad == null) {
            return false;
        }

        if (disponibilidad.getHoraInicio()
                .isAfter(disponibilidad.getHoraFin())) {
            return false;
        }

        if (disponibilidad.getIntervaloMinutos() <= 0) {
            return false;
        }

        if (disponibilidad.getDiasAtencion().isEmpty()) {
            return false;
        }

        if (disponibilidad.getSemanasHabilitadas() <= 0) {
            return false;
        }

        return true;
    }

    public List<FranjaHoraria> generarFranjas(DisponibilidadEspecialista disponibilidad) {

        List<FranjaHoraria> franjas = new ArrayList<>();

        LocalTime inicio = disponibilidad.getHoraInicio();
        LocalTime fin = disponibilidad.getHoraFin();
        int intervalo = disponibilidad.getIntervaloMinutos();

        while (inicio.plusMinutes(intervalo).compareTo(fin) <= 0) {

            FranjaHoraria franja = new FranjaHoraria(
                    inicio,
                    inicio.plusMinutes(intervalo),
                    true
            );

            franjas.add(franja);

            inicio = inicio.plusMinutes(intervalo);
        }

        return franjas;
    }
}
