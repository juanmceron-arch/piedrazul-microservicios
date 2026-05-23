package co.unicauca.appointment_service;

import co.unicauca.appointment_service.model.Cita;
import co.unicauca.appointment_service.model.EstadoCita;
import co.unicauca.appointment_service.repository.CitaRepository;
import co.unicauca.appointment_service.service.CancelarCitaServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CancelarCitaServicioTest {

    private CitaRepository repo;
    private CancelarCitaServicio servicio;

    @BeforeEach
    void setUp() {
        repo = mock(CitaRepository.class);
        servicio = new CancelarCitaServicio(repo);
    }

    // ------------------------------------------------------------------ //
    //  cancelar() — caso feliz                                             //
    // ------------------------------------------------------------------ //

    @Test
    void cancelarCambaiaEstadoDeCitaACANCELADA() {
        Cita cita = citaAgendada("CITA-001");
        when(repo.findById("CITA-001")).thenReturn(Optional.of(cita));
        when(repo.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio.cancelar("CITA-001");

        assertEquals(EstadoCita.CANCELADA, cita.getEstado());
    }

    @Test
    void cancelarPersisteLaCitaActualizada() {
        Cita cita = citaAgendada("CITA-002");
        when(repo.findById("CITA-002")).thenReturn(Optional.of(cita));
        when(repo.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio.cancelar("CITA-002");

        verify(repo).save(cita);
    }

    @Test
    void cancelarDevuelveMensajeDeExito() {
        Cita cita = citaAgendada("CITA-003");
        when(repo.findById("CITA-003")).thenReturn(Optional.of(cita));
        when(repo.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        String resultado = servicio.cancelar("CITA-003");

        assertEquals("Cita cancelada exitosamente", resultado);
    }

    // ------------------------------------------------------------------ //
    //  cancelar() — casos de error                                         //
    // ------------------------------------------------------------------ //

    @Test
    void cancelarLanzaExcepcionCuandoLaCitaNoExiste() {
        when(repo.findById("INEXISTENTE")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> servicio.cancelar("INEXISTENTE"));
    }

    @Test
    void cancelarNoLlamaASaveCuandoLaCitaNoExiste() {
        when(repo.findById("INEXISTENTE")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> servicio.cancelar("INEXISTENTE"));

        verify(repo, never()).save(any());
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    private Cita citaAgendada(String id) {
        return new Cita(
                id,
                1,
                "Paciente Prueba",
                "ESP-1",
                "Dr. Prueba",
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                60,
                EstadoCita.AGENDADA
        );
    }
}