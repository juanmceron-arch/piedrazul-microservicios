package com.piedrazul.pacientes.controlador;

import com.piedrazul.pacientes.modelo.Paciente;
import com.piedrazul.pacientes.servicio.PacienteServicio;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/pacientes", "/api/pacientes"})
public class PacienteControlador {

    private final PacienteServicio servicio;

    public PacienteControlador(PacienteServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Paciente> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Paciente> buscar(@PathVariable String id) {
        return servicio.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Paciente paciente) {
        try {
            Paciente guardado = servicio.guardar(paciente);
            return ResponseEntity.created(URI.create("/api/pacientes/" + guardado.getId()))
                    .body(guardado);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Paciente paciente) {
        if (paciente == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "El paciente es obligatorio"));
        }
        paciente.setId(id);
        try {
            return ResponseEntity.ok(servicio.guardar(paciente));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        if (!servicio.eliminar(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
