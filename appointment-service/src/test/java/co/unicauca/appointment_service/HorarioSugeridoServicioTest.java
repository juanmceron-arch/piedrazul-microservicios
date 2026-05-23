package co.unicauca.appointment_service;

import co.unicauca.appointment_service.client.DisponibilidadClient;
import co.unicauca.appointment_service.model.EstadoCita;
import co.unicauca.appointment_service.repository.CitaRepository;
import co.unicauca.appointment_service.service.HorarioSugeridoServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class HorarioSugeridoServicioTest {

    private CitaRepository repo;
    private DisponibilidadClient disponibilidadClient;
    private HorarioSugeridoServicio servicio;

    /** Próximo lunes a partir de mañana (siempre futuro). */
    private final LocalDate LUNES_FUTURO = LocalDate.now()
            .plusDays(1)
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

    @BeforeEach
    void setUp() {
        repo = mock(CitaRepository.class);
        disponibilidadClient = mock(DisponibilidadClient.class);
        servicio = new HorarioSugeridoServicio(repo, disponibilidadClient);
    }

    // ------------------------------------------------------------------ //
    //  Casos de fecha inválida — sin consultar disponibilidad              //
    // ------------------------------------------------------------------ //

    @Test
    void obtenerDevuelveListaVaciaCuandoFechaEsNull() {
        List<LocalTime> resultado = servicio.obtener("ESP-1", null);

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(disponibilidadClient);
    }

    @Test
    void obtenerDevuelveListaVaciaCuandoFechaEsEnElPasado() {
        LocalDate ayer = LocalDate.now().minusDays(1);

        List<LocalTime> resultado = servicio.obtener("ESP-1", ayer);

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(disponibilidadClient);
    }

    @Test
    void obtenerDevuelveListaVaciaCuandoFechaEsHoy() {
    // Simulamos cualquier respuesta del cliente, porque el servicio
    // ahora sí consulta la disponibilidad incluso cuando la fecha es hoy.
    when(disponibilidadClient.obtenerDisponibilidad("ESP-1"))
            .thenReturn(disponibilidadConDias(List.of("MONDAY"), 4));

    List<LocalTime> resultado = servicio.obtener("ESP-1", LocalDate.now());

    assertTrue(resultado.isEmpty());

    // Verificamos que sí se consultó el cliente.
    verify(disponibilidadClient).obtenerDisponibilidad("ESP-1");
}

    // ------------------------------------------------------------------ //
    //  Casos de disponibilidad vacía o nula                               //
    // ------------------------------------------------------------------ //

    @Test
    void obtenerDevuelveListaVaciaCuandoDisponibilidadEsNull() {
        when(disponibilidadClient.obtenerDisponibilidad("ESP-1")).thenReturn(null);

        List<LocalTime> resultado = servicio.obtener("ESP-1", LUNES_FUTURO);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void obtenerDevuelveListaVaciaCuandoDisponibilidadEstaVacia() {
        when(disponibilidadClient.obtenerDisponibilidad("ESP-1")).thenReturn(Map.of());

        List<LocalTime> resultado = servicio.obtener("ESP-1", LUNES_FUTURO);

        assertTrue(resultado.isEmpty());
    }

    // ------------------------------------------------------------------ //
    //  Semanas habilitadas                                                 //
    // ------------------------------------------------------------------ //

    @Test
    void obtenerDevuelveListaVaciaCuandoFechaExcedeSemanaHabilitada() {
        LocalDate fechaMuyLejana = LocalDate.now().plusWeeks(5);
        when(disponibilidadClient.obtenerDisponibilidad("ESP-1"))
                .thenReturn(disponibilidadConDias(List.of(fechaMuyLejana.getDayOfWeek().name()), 2));

        List<LocalTime> resultado = servicio.obtener("ESP-1", fechaMuyLejana);

        assertTrue(resultado.isEmpty());
    }

    // ------------------------------------------------------------------ //
    //  Día de atención                                                     //
    // ------------------------------------------------------------------ //

    @Test
    void obtenerDevuelveListaVaciaCuandoEspecialistaNoAtiendeEseDia() {
        // LUNES_FUTURO es lunes; la disponibilidad solo tiene MARTES
        when(disponibilidadClient.obtenerDisponibilidad("ESP-1"))
                .thenReturn(disponibilidadConDias(List.of("TUESDAY"), 4));

        List<LocalTime> resultado = servicio.obtener("ESP-1", LUNES_FUTURO);

        assertTrue(resultado.isEmpty());
    }

    // ------------------------------------------------------------------ //
    //  Horarios libres                                                     //
    // ------------------------------------------------------------------ //

    @Test
    void obtenerDevuelveHorariosLibresDentroDelRango() {
        // Disponibilidad: lunes, 08:00-10:00, intervalo 60 min → slots 08:00 y 09:00
        when(disponibilidadClient.obtenerDisponibilidad("ESP-1"))
                .thenReturn(disponibilidadConDias(List.of("MONDAY"), 4));
        when(repo.existsByEspecialistaIdAndFechaAndHoraAndEstadoNot(
                anyString(), any(LocalDate.class), any(LocalTime.class), eq(EstadoCita.CANCELADA)))
                .thenReturn(false);

        List<LocalTime> resultado = servicio.obtener("ESP-1", LUNES_FUTURO);

        assertEquals(2, resultado.size());
        assertTrue(resultado.contains(LocalTime.of(8, 0)));
        assertTrue(resultado.contains(LocalTime.of(9, 0)));
    }

    @Test
    void obtenerExcluyeHorariosOcupados() {
        // El slot 08:00 ya tiene cita
        when(disponibilidadClient.obtenerDisponibilidad("ESP-1"))
                .thenReturn(disponibilidadConDias(List.of("MONDAY"), 4));
        when(repo.existsByEspecialistaIdAndFechaAndHoraAndEstadoNot(
                "ESP-1", LUNES_FUTURO, LocalTime.of(8, 0), EstadoCita.CANCELADA))
                .thenReturn(true);
        when(repo.existsByEspecialistaIdAndFechaAndHoraAndEstadoNot(
                "ESP-1", LUNES_FUTURO, LocalTime.of(9, 0), EstadoCita.CANCELADA))
                .thenReturn(false);

        List<LocalTime> resultado = servicio.obtener("ESP-1", LUNES_FUTURO);

        assertEquals(1, resultado.size());
        assertEquals(LocalTime.of(9, 0), resultado.get(0));
    }

    @Test
    void obtenerDevuelveListaVaciaCuandoTodosLosHorariosEstanOcupados() {
        when(disponibilidadClient.obtenerDisponibilidad("ESP-1"))
                .thenReturn(disponibilidadConDias(List.of("MONDAY"), 4));
        when(repo.existsByEspecialistaIdAndFechaAndHoraAndEstadoNot(
                anyString(), any(LocalDate.class), any(LocalTime.class), eq(EstadoCita.CANCELADA)))
                .thenReturn(true);

        List<LocalTime> resultado = servicio.obtener("ESP-1", LUNES_FUTURO);

        assertTrue(resultado.isEmpty());
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    /**
     * Construye un mapa de disponibilidad con los días recibidos, franja 08:00-10:00,
     * intervalo 60 min y las semanas habilitadas indicadas.
     */
    private Map<String, Object> disponibilidadConDias(List<String> dias, int semanasHabilitadas) {
        return Map.of(
                "diasAtencion", dias,
                "horaInicio", "08:00",
                "horaFin", "10:00",
                "intervaloMinutos", 60,
                "semanasHabilitadas", semanasHabilitadas
        );
    }
}