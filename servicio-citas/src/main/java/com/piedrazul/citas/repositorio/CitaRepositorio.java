package com.piedrazul.citas.repositorio;

import com.piedrazul.citas.modelo.Cita;
import com.piedrazul.citas.modelo.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaRepositorio extends JpaRepository<Cita, String> {

    // Reemplaza tu existeCitaEnHorario() de SQLiteCitaRepositorio
    boolean existsByEspecialistaIdAndFechaAndHora(
        String especialistaId, LocalDate fecha, LocalTime hora
    );

    // Reemplaza tu listarTodasCitas()
    // findAll() ya viene gratis en JpaRepository, no necesitas declararlo

    // Filtrar por nombre de paciente
    List<Cita> findByPacienteNombreContainingIgnoreCase(String nombre);

    // Filtrar por estado
    List<Cita> findByEstadoCita(EstadoCita estado);
}
