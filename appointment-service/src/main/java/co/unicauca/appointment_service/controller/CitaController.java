package co.unicauca.appointment_service.controller;

import co.unicauca.appointment_service.dto.AgendarAgendadorRequest;
import co.unicauca.appointment_service.dto.AgendarPacienteRequest;
import co.unicauca.appointment_service.dto.ReagendarRequest;
import co.unicauca.appointment_service.model.EstadoCita;
import co.unicauca.appointment_service.service.AgendarAgendadorServicio;
import co.unicauca.appointment_service.service.AgendarPacienteServicio;
import co.unicauca.appointment_service.service.CambiarEstadoCitaServicio;
import co.unicauca.appointment_service.service.CancelarCitaServicio;
import co.unicauca.appointment_service.service.ConsultarCitaServicio;
import co.unicauca.appointment_service.service.HorarioSugeridoServicio;
import co.unicauca.appointment_service.service.ReagendarCitaServicio;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author Juan Martin
 */

@RestController
@RequestMapping("/citas")
@RequiredArgsConstructor
public class CitaController {
    private final AgendarPacienteServicio pacienteServicio;
    private final AgendarAgendadorServicio agendadorServicio;
    private final CancelarCitaServicio cancelarServicio;
    private final CambiarEstadoCitaServicio cambiarEstadoServicio;
    private final ReagendarCitaServicio reagendarServicio;
    private final ConsultarCitaServicio consultarServicio;
    private final HorarioSugeridoServicio horarioServicio;

    @PreAuthorize("hasRole('PACIENTE')")
    @PostMapping("/agendar/paciente")
    public ResponseEntity<?> agendarPaciente(@RequestBody AgendarPacienteRequest req){
        return ResponseEntity.ok(pacienteServicio.agendar(req));
    }

    @PreAuthorize("hasRole('AGENDADOR')")
    @PostMapping("/agendar/agendador")
    public ResponseEntity<?> agendarAgendador(@RequestBody AgendarAgendadorRequest req){
        return ResponseEntity.ok(agendadorServicio.agendar(req));
    }

    @PreAuthorize("hasAnyRole('AGENDADOR', 'PACIENTE')")
    @GetMapping
    public ResponseEntity<?> listar(){
        return ResponseEntity.ok(consultarServicio.listar());
    }

    @PreAuthorize("hasAnyRole('AGENDADOR', 'PACIENTE')")
    @PutMapping("/cancelar/{id}")
    public ResponseEntity<?> cancelar(@PathVariable String id){
        cancelarServicio.cancelar(id);
        return ResponseEntity.ok("Cancelada");
    }

    @PreAuthorize("hasRole('AGENDADOR')")
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable String id, @RequestParam EstadoCita estado){
        return ResponseEntity.ok(cambiarEstadoServicio.cambiar(id, estado));
    }

    @PreAuthorize("hasAnyRole('AGENDADOR', 'PACIENTE')")
    @PutMapping("/reagendar/{id}")
    public ResponseEntity<?> reagendar(@PathVariable String id,@RequestBody ReagendarRequest req){
        return ResponseEntity.ok(reagendarServicio.reagendar(id, req));
    }

    @PreAuthorize("hasAnyRole('AGENDADOR', 'PACIENTE')")
    @GetMapping("/horarios")
    public ResponseEntity<?> horarios(@RequestParam String especialistaId,@RequestParam LocalDate fecha){
        return ResponseEntity.ok(horarioServicio.obtener(especialistaId, fecha));
    }
}
