package com.piedrazul.citas.async.controlador;

import com.piedrazul.citas.async.repositorio.EspecialistaEspejoRepositorio;
import com.piedrazul.citas.cliente.PacienteCliente;
import com.piedrazul.citas.modelo.Cita;
import com.piedrazul.citas.modelo.EstadoCita;
import com.piedrazul.citas.repositorio.CitaRepositorio;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/citas/async")
@ConditionalOnProperty(prefix = "piedrazul.async", name = "enabled", havingValue = "true")
public class CitaAsyncControlador {

    private final CitaRepositorio repo;
    private final EspecialistaEspejoRepositorio especialistaEspejoRepositorio;
    private final PacienteCliente pacienteCliente;

    public CitaAsyncControlador(CitaRepositorio repo,
                                EspecialistaEspejoRepositorio especialistaEspejoRepositorio,
                                PacienteCliente pacienteCliente) {
        this.repo = repo;
        this.especialistaEspejoRepositorio = especialistaEspejoRepositorio;
        this.pacienteCliente = pacienteCliente;
    }

    @PostMapping
    public ResponseEntity<?> agendarConEspejoAsincrono(@RequestBody Cita cita) {
        ResponseEntity<?> errorValidacion = validarDatosCita(cita);
        if (errorValidacion != null) {
            return errorValidacion;
        }

        if (!especialistaEspejoRepositorio.existe(cita.getEspecialistaId())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El especialista no existe en el espejo asincrono de servicio-citas"));
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
