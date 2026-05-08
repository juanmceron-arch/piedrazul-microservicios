package com.piedrazul.citas.controlador;

import com.piedrazul.citas.cliente.EspecialistaCliente;
import com.piedrazul.citas.cliente.PacienteCliente;
import com.piedrazul.citas.modelo.Cita;
import com.piedrazul.citas.modelo.EstadoCita;
import com.piedrazul.citas.repositorio.CitaRepositorio;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        ResponseEntity<?> errorValidacion = validarDatosCita(cita);
        if (errorValidacion != null) {
            return errorValidacion;
        }

        if (!especialistaCliente.existeEspecialista(cita.getEspecialistaId())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "El especialista no existe o especialista-service no esta disponible"));
        }

        if (!pacienteCliente.existePaciente(cita.getPacienteId())) {
            boolean pacienteCreado = pacienteCliente.crearPaciente(datosPaciente(cita));
            if (!pacienteCreado) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "No se pudo validar o registrar el paciente en paciente-service"));
            }
        }

        boolean ocupado = repo.existsByEspecialistaIdAndFechaAndHora(
            cita.getEspecialistaId(), cita.getFecha(), cita.getHora()
        );
        if (ocupado) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "La hora seleccionada no esta disponible"));
        }

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

    private ResponseEntity<?> validarDatosCita(Cita cita) {
        if (cita == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "La cita no puede estar vacia"));
        }
        if (textoVacio(cita.getPacienteId()) || textoVacio(cita.getPacienteNombre())) {
            return ResponseEntity.badRequest().body(Map.of("error", "La cita debe incluir pacienteId y pacienteNombre"));
        }
        if (textoVacio(cita.getEspecialistaId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "La cita debe incluir especialistaId"));
        }
        if (cita.getFecha() == null || cita.getHora() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "La cita debe incluir fecha y hora"));
        }
        if (cita.getDuracionMinutos() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "La duracion de la cita debe ser mayor que cero"));
        }
        return null;
    }

    private Map<String, String> datosPaciente(Cita cita) {
        Map<String, String> datos = new HashMap<>();
        datos.put("id", cita.getPacienteId());
        datos.put("nombre", cita.getPacienteNombre());
        datos.put("apellido", valorSeguro(cita.getPacienteApellido()));
        datos.put("telefono", valorSeguro(cita.getPacienteTelefono()));
        datos.put("genero", valorSeguro(cita.getPacienteGenero()));
        return datos;
    }

    private String valorSeguro(String valor) {
        return valor == null ? "" : valor;
    }

    private boolean textoVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
