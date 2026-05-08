package co.unicauca.especialista_service.controller;

import co.unicauca.especialista_service.DTO.ConfigurarDisponibilidadDto;
import co.unicauca.especialista_service.builder.DisponibilidadEspecialistaBuilder;
import co.unicauca.especialista_service.model.DisponibilidadEspecialista;
import co.unicauca.especialista_service.service.DisponibilidadService;
import java.time.DayOfWeek;
import java.time.LocalTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Juan Martin
 */
@RestController
@RequestMapping({"/disponibilidad", "/api/disponibilidad"})
public class DisponibilidadController {
    
    private final DisponibilidadService service;

    public DisponibilidadController(DisponibilidadService service) {
        this.service = service;
    }
    
    @PostMapping("/{especialistaId}")
    public String configurar(
            @PathVariable String especialistaId,
            @RequestBody ConfigurarDisponibilidadDto dto
    ) {

        DisponibilidadEspecialista disponibilidad =
                new DisponibilidadEspecialistaBuilder()
                        .diasAtencion(
                                dto.getDiasAtencion()
                                        .stream()
                                        .map(DayOfWeek::valueOf)
                                        .toList()
                        )
                        .horaInicio(LocalTime.parse(dto.getHoraInicio()))
                        .horaFin(LocalTime.parse(dto.getHoraFin()))
                        .intervaloMinutos(dto.getIntervaloMinutos())
                        .semanasHabilitadas(dto.getSemanasHabilitadas())
                        .build();

        service.configurarDisponibilidad(
                especialistaId,
                disponibilidad
        );

        return "Disponibilidad configurada";
    }
    
    @GetMapping("/{especialistaId}")
    public DisponibilidadEspecialista consultar(@PathVariable String especialistaId) {
        return service.consultarDisponibilidad(especialistaId);
    }
}
