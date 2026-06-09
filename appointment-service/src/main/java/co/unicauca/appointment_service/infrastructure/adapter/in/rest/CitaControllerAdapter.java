package co.unicauca.appointment_service.infrastructure.adapter.in.rest;

import co.unicauca.appointment_service.domain.model.EstadoCita;
import co.unicauca.appointment_service.domain.port.in.*;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Adaptador de entrada REST.
 * Solo traduce HTTP ↔ comandos de dominio; no contiene lógica de negocio.
 */
@RestController
@RequestMapping("/citas")
@RequiredArgsConstructor
public class CitaControllerAdapter {

    private final AgendarCitaUseCase agendarUseCase;
    private final CancelarCitaUseCase cancelarUseCase;
    private final ReagendarCitaUseCase reagendarUseCase;
    private final CambiarEstadoCitaUseCase cambiarEstadoUseCase;
    private final ConsultarCitaUseCase consultarUseCase;
    private final ConsultarHorariosUseCase horariosUseCase;

    @PreAuthorize("hasRole('PACIENTE')")
    @PostMapping("/agendar/paciente")
    public ResponseEntity<?> agendarPaciente(@RequestBody CitaRequest.AgendarPaciente req) {
        var cmd = new AgendarCitaUseCase.ComandoPaciente(
                req.getPacienteId(), req.getEspecialistaId(), req.getFecha(), req.getHora());
        return ResponseEntity.ok(agendarUseCase.agendarComoPaciente(cmd));
    }

    @PreAuthorize("hasRole('AGENDADOR')")
    @PostMapping("/agendar/agendador")
    public ResponseEntity<?> agendarAgendador(@RequestBody CitaRequest.AgendarAgendador req) {
        var cmd = new AgendarCitaUseCase.ComandoAgendador(
                req.getPacienteId(), req.getNombrePaciente(), req.getApellidoPaciente(),
                req.getTelefono(), req.getFechaNacimiento(), req.getCorreo(), req.getGenero(),
                req.getEspecialistaId(), req.getFecha(), req.getHora());
        return ResponseEntity.ok(agendarUseCase.agendarComoAgendador(cmd));
    }

    @PreAuthorize("hasAnyRole('AGENDADOR', 'PACIENTE')")
    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(consultarUseCase.listar());
    }

    @PreAuthorize("hasAnyRole('AGENDADOR', 'PACIENTE')")
    @PutMapping("/cancelar/{id}")
    public ResponseEntity<?> cancelar(@PathVariable String id) {
        return ResponseEntity.ok(cancelarUseCase.cancelar(id));
    }

    @PreAuthorize("hasRole('AGENDADOR')")
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable String id,
                                            @RequestParam EstadoCita estado) {
        return ResponseEntity.ok(cambiarEstadoUseCase.cambiar(id, estado));
    }

    @PreAuthorize("hasAnyRole('AGENDADOR', 'PACIENTE')")
    @PutMapping("/reagendar/{id}")
    public ResponseEntity<?> reagendar(@PathVariable String id,
                                        @RequestBody CitaRequest.Reagendar req) {
        var cmd = new ReagendarCitaUseCase.Comando(req.getFecha(), req.getHora());
        return ResponseEntity.ok(reagendarUseCase.reagendar(id, cmd));
    }

    @PreAuthorize("hasAnyRole('AGENDADOR', 'PACIENTE')")
    @GetMapping("/horarios")
    public ResponseEntity<?> horarios(@RequestParam String especialistaId,
                                       @RequestParam LocalDate fecha) {
        return ResponseEntity.ok(horariosUseCase.obtenerDisponibles(especialistaId, fecha));
    }
}
