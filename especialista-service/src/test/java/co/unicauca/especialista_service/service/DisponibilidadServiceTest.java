package co.unicauca.especialista_service.service;

import co.unicauca.especialista_service.builder.DisponibilidadEspecialistaBuilder;
import co.unicauca.especialista_service.model.DisponibilidadEspecialista;
import co.unicauca.especialista_service.repository.SQLDisponibilidadRepositorio;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 * @author Juan Martin
 */
public class DisponibilidadServiceTest {
    private SQLDisponibilidadRepositorio repositorio;
    private DisponibilidadService service;

    @BeforeEach
    void setUp() {
        repositorio = mock(SQLDisponibilidadRepositorio.class);
        service = new DisponibilidadService(repositorio);
    }

    @Test
    void debeConfigurarDisponibilidad() {
        DisponibilidadEspecialista disponibilidad =
                new DisponibilidadEspecialistaBuilder()
                        .diasAtencion(List.of(DayOfWeek.MONDAY))
                        .horaInicio(LocalTime.of(8, 0))
                        .horaFin(LocalTime.of(12, 0))
                        .intervaloMinutos(30)
                        .semanasHabilitadas(4)
                        .build();

        service.configurarDisponibilidad("esp1", disponibilidad);

        verify(repositorio, times(1)).guardar("esp1", disponibilidad);
    }
}
