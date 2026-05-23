package co.unicauca.appointment_service;

import co.unicauca.appointment_service.client.EspecialistaClient;
import co.unicauca.appointment_service.client.PacienteClient;
import co.unicauca.appointment_service.dto.AgendarPacienteRequest;
import co.unicauca.appointment_service.model.Cita;
import co.unicauca.appointment_service.model.EstadoCita;
import co.unicauca.appointment_service.repository.CitaRepository;
import co.unicauca.appointment_service.service.AgendarPacienteServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AgendarPacienteServicioTest {

    private CitaRepository repo;
    private PacienteClient pacienteClient;
    private EspecialistaClient especialistaClient;
    private AgendarPacienteServicio servicio;

    @BeforeEach
    void setUp() {
        repo = mock(CitaRepository.class);
        pacienteClient = mock(PacienteClient.class);
        especialistaClient = mock(EspecialistaClient.class);
        servicio = new AgendarPacienteServicio(repo, pacienteClient, especialistaClient);
    }

    @Test
    void agendarUsaBuilderYPersisteCitaConDatosDePacienteYEspecialista() {
        AgendarPacienteRequest request = request(LocalDate.now().plusDays(1), LocalTime.of(8, 0));
        request.setPacienteId(10);
        request.setEspecialistaId("ESP-10");

        when(repo.existsByEspecialistaIdAndFechaAndHoraAndEstadoNot(
                request.getEspecialistaId(), request.getFecha(), request.getHora(), EstadoCita.CANCELADA
        )).thenReturn(false);
        when(pacienteClient.obtenerPaciente(10)).thenReturn(Map.of("nombre", "Carlos", "apellido", "Diaz"));
        when(especialistaClient.obtenerEspecialista("ESP-10")).thenReturn(Map.of("nombre", "Dra. Lopez"));
        when(repo.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cita cita = servicio.agendar(request);

        assertEquals(10, cita.getPacienteId());
        assertEquals("Carlos Diaz", cita.getPacienteNombre());
        assertEquals("ESP-10", cita.getEspecialistaId());
        assertEquals("Dra. Lopez", cita.getEspecialistaNombre());
        assertEquals(request.getFecha(), cita.getFecha());
        assertEquals(request.getHora(), cita.getHora());
        assertEquals(60, cita.getDuracion());
        assertEquals(EstadoCita.AGENDADA, cita.getEstado());
        verify(repo).save(any(Cita.class));
    }

    @Test
    void agendarNoCreaCitaCuandoElHorarioEstaOcupado() {
        AgendarPacienteRequest request = request(LocalDate.now().plusDays(1), LocalTime.of(8, 0));

        when(repo.existsByEspecialistaIdAndFechaAndHoraAndEstadoNot(
                request.getEspecialistaId(), request.getFecha(), request.getHora(), EstadoCita.CANCELADA
        )).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> servicio.agendar(request));

        assertTrue(exception.getMessage().contains("Horario ocupado"));
        verify(repo, never()).save(any(Cita.class));
    }

    @Test
    void agendarNoCreaCitaCuandoFechaEsEnElPasado() {
        AgendarPacienteRequest request = request(LocalDate.now().minusDays(1), LocalTime.of(9, 0));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> servicio.agendar(request));

        assertTrue(ex.getMessage().contains("fechas pasadas"));
        verify(repo, never()).save(any());
    }

    @Test
    void agendarUsaNombreFallbackCuandoPacienteNoTieneNombre() {
        AgendarPacienteRequest request = request(LocalDate.now().plusDays(1), LocalTime.of(9, 0));

        Map<String, Object> pacienteSinNombre = new HashMap<>();
        pacienteSinNombre.put("nombre", null);
        pacienteSinNombre.put("apellido", null);

        when(repo.existsByEspecialistaIdAndFechaAndHoraAndEstadoNot(any(), any(), any(), any())).thenReturn(false);
        when(pacienteClient.obtenerPaciente(request.getPacienteId())).thenReturn(pacienteSinNombre);
        when(especialistaClient.obtenerEspecialista(request.getEspecialistaId())).thenReturn(Map.of("nombre", "Dr. Test"));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Cita cita = servicio.agendar(request);

        assertEquals("Paciente", cita.getPacienteNombre());
    }

    @Test
    void agendarUsaFallbackCuandoEspecialistaNoTieneNombre() {
        AgendarPacienteRequest request = request(LocalDate.now().plusDays(1), LocalTime.of(9, 0));

        when(repo.existsByEspecialistaIdAndFechaAndHoraAndEstadoNot(any(), any(), any(), any())).thenReturn(false);
        when(pacienteClient.obtenerPaciente(request.getPacienteId()))
                .thenReturn(Map.of("nombre", "Juan", "apellido", "Perez"));
        when(especialistaClient.obtenerEspecialista(request.getEspecialistaId())).thenReturn(Map.of());
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Cita cita = servicio.agendar(request);

        assertEquals("Especialista", cita.getEspecialistaNombre());
    }

    @Test
    void agendarCreaLaCitaConEstadoAGENDADA() {
        AgendarPacienteRequest request = request(LocalDate.now().plusDays(1), LocalTime.of(9, 0));

        when(repo.existsByEspecialistaIdAndFechaAndHoraAndEstadoNot(any(), any(), any(), any())).thenReturn(false);
        when(pacienteClient.obtenerPaciente(request.getPacienteId()))
                .thenReturn(Map.of("nombre", "Ana", "apellido", "Diaz"));
        when(especialistaClient.obtenerEspecialista(request.getEspecialistaId()))
                .thenReturn(Map.of("nombre", "Dra. Ruiz"));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Cita cita = servicio.agendar(request);

        assertEquals(EstadoCita.AGENDADA, cita.getEstado());
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    private AgendarPacienteRequest request(LocalDate fecha, LocalTime hora) {
        AgendarPacienteRequest req = new AgendarPacienteRequest();
        req.setPacienteId(5);
        req.setEspecialistaId("ESP-5");
        req.setFecha(fecha);
        req.setHora(hora);
        return req;
    }
}