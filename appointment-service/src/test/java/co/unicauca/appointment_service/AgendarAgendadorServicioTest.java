package co.unicauca.appointment_service;

import co.unicauca.appointment_service.client.EspecialistaClient;
import co.unicauca.appointment_service.dto.AgendarAgendadorRequest;
import co.unicauca.appointment_service.model.Cita;
import co.unicauca.appointment_service.model.EstadoCita;
import co.unicauca.appointment_service.repository.CitaRepository;
import co.unicauca.appointment_service.service.AgendarAgendadorServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AgendarAgendadorServicioTest {

    private CitaRepository repo;
    private EspecialistaClient especialistaClient;
    private AgendarAgendadorServicio servicio;

    @BeforeEach
    void setUp() {
        repo = mock(CitaRepository.class);
        especialistaClient = mock(EspecialistaClient.class);
        servicio = new AgendarAgendadorServicio(repo, especialistaClient);
    }

    @Test
    void agendarConstruyeCitaDesdeDatosIngresadosPorElAgendador() {
        AgendarAgendadorRequest request = request(LocalDate.now().plusDays(3));
        request.setPacienteId(25);
        request.setNombrePaciente("Maria");
        request.setApellidoPaciente("Quintero");
        request.setEspecialistaId("ESP-25");
        request.setHora(LocalTime.of(14, 0));

        when(repo.existsByEspecialistaIdAndFechaAndHoraAndEstadoNot(
                request.getEspecialistaId(), request.getFecha(), request.getHora(), EstadoCita.CANCELADA
        )).thenReturn(false);
        when(especialistaClient.obtenerEspecialista("ESP-25")).thenReturn(Map.of("nombre", "Dr. Paredes"));
        when(repo.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cita cita = servicio.agendar(request);

        assertEquals(25, cita.getPacienteId());
        assertEquals("Maria Quintero", cita.getPacienteNombre());
        assertEquals("ESP-25", cita.getEspecialistaId());
        assertEquals("Dr. Paredes", cita.getEspecialistaNombre());
        assertEquals(request.getFecha(), cita.getFecha());
        assertEquals(request.getHora(), cita.getHora());
        assertEquals(EstadoCita.AGENDADA, cita.getEstado());
        verify(repo).save(any(Cita.class));
    }

    @Test
    void agendarLanzaExcepcionCuandoFechaEsPasada() {
        AgendarAgendadorRequest request = request(LocalDate.now().minusDays(2));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> servicio.agendar(request));

        assertTrue(ex.getMessage().contains("fechas pasadas"));
        verify(repo, never()).save(any());
    }

    @Test
    void agendarLanzaExcepcionCuandoHorarioEstaOcupado() {
        AgendarAgendadorRequest request = request(LocalDate.now().plusDays(1));

        when(repo.existsByEspecialistaIdAndFechaAndHoraAndEstadoNot(
                request.getEspecialistaId(), request.getFecha(), request.getHora(), EstadoCita.CANCELADA
        )).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> servicio.agendar(request));

        assertTrue(ex.getMessage().contains("Horario ocupado"));
        verify(repo, never()).save(any());
    }

    @Test
    void agendarConcatenaCorrectamenteNombreYApellidoNulos() {
        AgendarAgendadorRequest request = request(LocalDate.now().plusDays(1));
        request.setNombrePaciente(null);
        request.setApellidoPaciente(null);

        when(repo.existsByEspecialistaIdAndFechaAndHoraAndEstadoNot(any(), any(), any(), any())).thenReturn(false);
        when(especialistaClient.obtenerEspecialista(request.getEspecialistaId())).thenReturn(Map.of("nombre", "Dr. X"));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Cita cita = servicio.agendar(request);

        assertEquals("", cita.getPacienteNombre());
    }

    @Test
    void agendarCreaLaCitaConEstadoAGENDADA() {
        AgendarAgendadorRequest request = request(LocalDate.now().plusDays(1));

        when(repo.existsByEspecialistaIdAndFechaAndHoraAndEstadoNot(any(), any(), any(), any())).thenReturn(false);
        when(especialistaClient.obtenerEspecialista(request.getEspecialistaId()))
                .thenReturn(Map.of("nombre", "Dra. Soto"));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Cita cita = servicio.agendar(request);

        assertEquals(EstadoCita.AGENDADA, cita.getEstado());
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    private AgendarAgendadorRequest request(LocalDate fecha) {
        AgendarAgendadorRequest req = new AgendarAgendadorRequest();
        req.setPacienteId(30);
        req.setNombrePaciente("Pedro");
        req.setApellidoPaciente("Salazar");
        req.setEspecialistaId("ESP-30");
        req.setFecha(fecha);
        req.setHora(LocalTime.of(10, 0));
        return req;
    }
}