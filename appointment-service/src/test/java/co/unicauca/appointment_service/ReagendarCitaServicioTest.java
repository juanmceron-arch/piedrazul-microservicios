package co.unicauca.appointment_service;

import co.unicauca.appointment_service.dto.ReagendarRequest;
import co.unicauca.appointment_service.model.Cita;
import co.unicauca.appointment_service.model.EstadoCita;
import co.unicauca.appointment_service.repository.CitaRepository;
import co.unicauca.appointment_service.service.ReagendarCitaServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReagendarCitaServicioTest {

    private CitaRepository repo;
    private ReagendarCitaServicio servicio;

    @BeforeEach
    void setUp() {
        repo = mock(CitaRepository.class);
        servicio = new ReagendarCitaServicio(repo);
    }

    // ------------------------------------------------------------------ //
    //  reagendar() — caso feliz                                            //
    // ------------------------------------------------------------------ //

    @Test
    void reagendarActualizaFechaYHoraDeLaCita() {
        Cita cita = citaAgendada("CITA-10");
        LocalDate nuevaFecha = LocalDate.now().plusDays(5);
        LocalTime nuevaHora = LocalTime.of(11, 0);

        when(repo.findById("CITA-10")).thenReturn(Optional.of(cita));
        when(repo.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        ReagendarRequest req = new ReagendarRequest();
        req.setFecha(nuevaFecha);
        req.setHora(nuevaHora);

        servicio.reagendar("CITA-10", req);

        assertEquals(nuevaFecha, cita.getFecha());
        assertEquals(nuevaHora, cita.getHora());
    }

    @Test
    void reagendarCambaiaEstadoAREAGENDADA() {
        Cita cita = citaAgendada("CITA-11");
        when(repo.findById("CITA-11")).thenReturn(Optional.of(cita));
        when(repo.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        ReagendarRequest req = new ReagendarRequest();
        req.setFecha(LocalDate.now().plusDays(7));
        req.setHora(LocalTime.of(15, 30));

        servicio.reagendar("CITA-11", req);

        assertEquals(EstadoCita.REAGENDADA, cita.getEstado());
    }

    @Test
    void reagendarPersisteLaCitaActualizada() {
        Cita cita = citaAgendada("CITA-12");
        when(repo.findById("CITA-12")).thenReturn(Optional.of(cita));
        when(repo.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        ReagendarRequest req = new ReagendarRequest();
        req.setFecha(LocalDate.now().plusDays(3));
        req.setHora(LocalTime.of(10, 0));

        servicio.reagendar("CITA-12", req);

        verify(repo).save(cita);
    }

    // ------------------------------------------------------------------ //
    //  reagendar() — casos de error                                        //
    // ------------------------------------------------------------------ //

    @Test
    void reagendarLanzaExcepcionCuandoLaCitaNoExiste() {
        when(repo.findById("INEXISTENTE")).thenReturn(Optional.empty());

        ReagendarRequest req = new ReagendarRequest();
        req.setFecha(LocalDate.now().plusDays(2));
        req.setHora(LocalTime.of(9, 0));

        assertThrows(NoSuchElementException.class, () -> servicio.reagendar("INEXISTENTE", req));
    }

    @Test
    void reagendarNoLlamaASaveCuandoLaCitaNoExiste() {
        when(repo.findById("INEXISTENTE")).thenReturn(Optional.empty());

        ReagendarRequest req = new ReagendarRequest();
        req.setFecha(LocalDate.now().plusDays(2));
        req.setHora(LocalTime.of(9, 0));

        assertThrows(NoSuchElementException.class, () -> servicio.reagendar("INEXISTENTE", req));

        verify(repo, never()).save(any());
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    private Cita citaAgendada(String id) {
        return new Cita(
                id,
                2,
                "Paciente Reagendar",
                "ESP-2",
                "Dr. Reagendar",
                LocalDate.now().plusDays(1),
                LocalTime.of(8, 0),
                60,
                EstadoCita.AGENDADA
        );
    }
}