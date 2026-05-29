package co.unicauca.appointment_service.service;

import co.unicauca.appointment_service.model.Cita;
import co.unicauca.appointment_service.model.EstadoCita;
import co.unicauca.appointment_service.repository.CitaRepository;
import co.unicauca.appointment_service.validation.ContextoValidacion;
import co.unicauca.appointment_service.validation.ValidadorCita;
import co.unicauca.appointment_service.validation.ValidadorFechaPasada;
import co.unicauca.appointment_service.validation.ValidadorHorarioOcupado;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Template Method (patron de comportamiento GoF).
 *
 * Define el esqueleto invariable del proceso de agendamiento:
 *   1. validar (delegado a la cadena de validacion - Chain of Responsibility)
 *   2. resolver el nombre del paciente      (paso variable)
 *   3. resolver el nombre del especialista  (paso variable)
 *   4. construir la cita
 *   5. persistir la cita
 *
 * Los pasos 2 y 3 son operaciones primitivas que cada subclase concreta
 * implementa segun su origen de datos (peticion directa del agendador o
 * consulta a otros microservicios). El metodo plantilla {@code agendarCita}
 * es final para garantizar que el flujo no cambie.
 *
 * @author Juan Martin
 */
public abstract class AgendarCitaTemplate {

    protected final CitaRepository repo;
    private final ValidadorCita cadenaValidacion;

    protected AgendarCitaTemplate(CitaRepository repo) {
        this.repo = repo;

        // Construccion de la cadena de validacion (Chain of Responsibility):
        // fecha pasada -> horario ocupado. Mismo orden que la logica original.
        ValidadorCita validadorFecha = new ValidadorFechaPasada();
        ValidadorCita validadorOcupado = new ValidadorHorarioOcupado(repo);
        validadorFecha.enlazarCon(validadorOcupado);
        this.cadenaValidacion = validadorFecha;
    }

    /**
     * Metodo plantilla. Define el algoritmo y no puede ser sobrescrito.
     */
    protected final Cita agendarCita(int pacienteId,
                                     String nombre,
                                     String apellido,
                                     String especialistaId,
                                     LocalDate fecha,
                                     LocalTime hora) {

        cadenaValidacion.validar(new ContextoValidacion(especialistaId, fecha, hora));

        String pacienteNombre = resolverNombrePaciente(pacienteId, nombre, apellido);
        String especialistaNombre = resolverNombreEspecialista(especialistaId);

        Cita cita = new Cita(
                UUID.randomUUID().toString(),
                pacienteId,
                pacienteNombre,
                especialistaId,
                especialistaNombre,
                fecha,
                hora,
                60,
                EstadoCita.AGENDADA
        );

        return repo.save(cita);
    }

    /**
     * Operacion primitiva: cada subclase obtiene el nombre del paciente
     * a partir de la fuente que le corresponde.
     */
    protected abstract String resolverNombrePaciente(int pacienteId, String nombre, String apellido);

    /**
     * Operacion primitiva: cada subclase obtiene el nombre del especialista.
     */
    protected abstract String resolverNombreEspecialista(String especialistaId);

    /**
     * Utilidad compartida por las subclases.
     */
    protected String valorComoTexto(java.util.Map<String, Object> map, String key, String fallback) {
        if (map == null || map.get(key) == null) return fallback;
        return String.valueOf(map.get(key));
    }
}
