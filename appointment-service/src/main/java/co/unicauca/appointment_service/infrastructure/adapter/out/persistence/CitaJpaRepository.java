package co.unicauca.appointment_service.infrastructure.adapter.out.persistence;

import co.unicauca.appointment_service.domain.model.EstadoCita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data JPA (solo infraestructura). */
interface CitaJpaRepository extends JpaRepository<CitaJpaEntity, String> {

    List<CitaJpaEntity> findByPacienteId(int pacienteId);

    boolean existsByEspecialistaIdAndFechaAndHoraAndEstadoNot(
            String especialistaId, LocalDate fecha, LocalTime hora, EstadoCita estadoExcluido);
}
