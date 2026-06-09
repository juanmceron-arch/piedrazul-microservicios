package co.unicauca.appointment_service.domain.port.in;

import co.unicauca.appointment_service.domain.model.Cita;
import java.util.List;

/** Puerto de entrada: consultar citas. */
public interface ConsultarCitaUseCase {
    List<Cita> listar();
}
