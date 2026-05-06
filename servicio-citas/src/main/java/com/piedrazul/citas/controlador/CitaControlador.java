package com.piedrazul.citas.controlador;

import com.piedrazul.citas.cliente.EspecialistaCliente;
import com.piedrazul.citas.cliente.PacienteCliente;
import com.piedrazul.citas.modelo.Cita;
import com.piedrazul.citas.modelo.EstadoCita;
import com.piedrazul.citas.repositorio.CitaRepositorio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
public class CitaControlador {

    private final CitaRepositorio repo;
    private final EspecialistaCliente especialistaCliente;
    private final PacienteCliente pacienteCliente;

    public CitaControlador(CitaRepositorio repo,
                           EspecialistaCliente especialistaCliente,
                           PacienteCliente pacienteCliente) {
        this.repo = repo;
        this.especialistaCliente = especialistaCliente;
        this.pacienteCliente = pacienteCliente;
    }

    @GetMapping
    public List<Cita> listarTodas() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> buscarPorId(@PathVariable String id) {
        return repo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> agendar(@RequestBody Cita cita) {

        // 1. Valida que el especialista existe
        if (!especialistaCliente.existeEspecialista(cita.getEspecialistaId())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "El especialista no existe"));
        }

        // 2. Si el paciente no existe, lo registra automáticamente
        if (!pacienteCliente.existePaciente(cita.getPacienteId())) {
            pacienteCliente.crearPaciente(Map.of(
                "id",             cita.getPacienteId(),
                "nombre",         cita.getPacienteNombre(),
                "apellido",       cita.getPacienteApellido(),
                "telefono",       cita.getPacienteTelefono(),
                "genero",         cita.getPacienteGenero()
            ));
        }

        // 3. Valida que el horario esté disponible
        boolean ocupado = repo.existsByEspecialistaIdAndFechaAndHora(
            cita.getEspecialistaId(), cita.getFecha(), cita.getHora()
        );
        if (ocupado) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "La hora seleccionada no está disponible"));
        }

        // 4. Guarda la cita
        cita.setEstadoCita(EstadoCita.AGENDADA);
        return ResponseEntity.ok(repo.save(cita));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable String id) {
        return repo.findById(id).map(cita -> {
            cita.setEstadoCita(EstadoCita.CANCELADA);
            return ResponseEntity.ok(repo.save(cita));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<?> reagendar(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        return repo.findById(id).map(cita -> {
            cita.setFecha(LocalDate.parse(body.get("fecha")));
            cita.setHora(LocalTime.parse(body.get("hora")));
            cita.setEstadoCita(EstadoCita.REAGENDADA);
            return ResponseEntity.ok(repo.save(cita));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public List<Cita> filtrar(@RequestParam(required = false) String paciente) {
        if (paciente != null && !paciente.isBlank()) {
            return repo.findByPacienteNombreContainingIgnoreCase(paciente);
        }
        return repo.findAll();
    }
}