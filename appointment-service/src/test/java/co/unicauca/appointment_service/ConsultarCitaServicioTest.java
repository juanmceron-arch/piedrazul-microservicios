package co.unicauca.appointment_service;

import co.unicauca.appointment_service.model.Cita;
import co.unicauca.appointment_service.model.EstadoCita;
import co.unicauca.appointment_service.repository.CitaRepository;
import co.unicauca.appointment_service.service.ConsultarCitaServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultarCitaServicioTest {

    private CitaRepository repo;
    private ConsultarCitaServicio servicio;

    @BeforeEach
    void setUp() {
        repo = mock(CitaRepository.class);
        servicio = new ConsultarCitaServicio(repo);
    }

    @Test
    void listarDevuelveTodasLasCitasDelRepositorio() {
        List<Cita> esperadas = List.of(
                cita("C-1", EstadoCita.AGENDADA),
                cita("C-2", EstadoCita.CANCELADA),
                cita("C-3", EstadoCita.REAGENDADA)
        );
        when(repo.findAll()).thenReturn(esperadas);

        List<Cita> resultado = servicio.listar();

        assertEquals(3, resultado.size());
        assertEquals(esperadas, resultado);
    }

    @Test
    void listarDevuelveListaVaciaCuandoNoHayCitas() {
        when(repo.findAll()).thenReturn(List.of());

        List<Cita> resultado = servicio.listar();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void listarDelegaEncontrarAlRepositorio() {
        when(repo.findAll()).thenReturn(List.of());

        servicio.listar();

        verify(repo, times(1)).findAll();
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    private Cita cita(String id, EstadoCita estado) {
        return new Cita(
                id,
                1,
                "Paciente Test",
                "ESP-1",
                "Dr. Test",
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                60,
                estado
        );
    }
}