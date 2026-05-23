package co.unicauca.appointment_service;

import co.unicauca.appointment_service.builder.CitaAgendadaBuilder;
import co.unicauca.appointment_service.model.Cita;
import co.unicauca.appointment_service.model.EstadoCita;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CitaAgendadaBuilderTest {

    @Test
    void buildCreaCitaAgendadaConCamposObligatoriosYValoresPorDefecto() {
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime hora = LocalTime.of(9, 0);

        Cita cita = new CitaAgendadaBuilder()
                .conPaciente(1, "Ana Perez")
                .conEspecialista("MED-1", "Dr. Gomez")
                .conFecha(fecha)
                .conHora(hora)
                .build();

        assertNotNull(cita.getId());
        assertEquals(1, cita.getPacienteId());
        assertEquals("Ana Perez", cita.getPacienteNombre());
        assertEquals("MED-1", cita.getEspecialistaId());
        assertEquals("Dr. Gomez", cita.getEspecialistaNombre());
        assertEquals(fecha, cita.getFecha());
        assertEquals(hora, cita.getHora());
        assertEquals(60, cita.getDuracion());
        assertEquals(EstadoCita.AGENDADA, cita.getEstado());
    }

    @Test
    void buildPermitePersonalizarDuracionEIdCuandoElProyectoLoRequiera() {
        Cita cita = new CitaAgendadaBuilder()
                .conId("CITA-001")
                .conPaciente(2, "Luis Mora")
                .conEspecialista("MED-2", "Dra. Rojas")
                .conFecha(LocalDate.now().plusDays(2))
                .conHora(LocalTime.of(10, 30))
                .conDuracion(30)
                .build();

        assertEquals("CITA-001", cita.getId());
        assertEquals(30, cita.getDuracion());
    }

    @Test
    void buildLanzaErrorCuandoFaltaUnCampoObligatorio() {
        CitaAgendadaBuilder builder = new CitaAgendadaBuilder()
                .conPaciente(1, "Ana Perez")
                .conFecha(LocalDate.now().plusDays(1))
                .conHora(LocalTime.of(9, 0));

        assertThrows(IllegalArgumentException.class, builder::build);
    }
}
