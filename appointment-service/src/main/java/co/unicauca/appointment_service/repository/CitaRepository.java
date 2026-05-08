package co.unicauca.appointment_service.repository;

import co.unicauca.appointment_service.model.Cita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Juan Martin
 */
public interface CitaRepository extends JpaRepository<Cita, String>{
    
    boolean existsByEspecialistaIdAndFechaAndHora(String especialistaId,LocalDate fecha,LocalTime hora);

    List<Cita> findByFechaAndEspecialistaId(LocalDate fecha,String especialistaId);
    
}
