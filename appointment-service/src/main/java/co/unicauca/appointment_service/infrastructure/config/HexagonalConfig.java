package co.unicauca.appointment_service.infrastructure.config;

import co.unicauca.appointment_service.application.usecase.*;
import co.unicauca.appointment_service.domain.port.in.*;
import co.unicauca.appointment_service.domain.port.out.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración central de la arquitectura hexagonal.
 * Aquí se cablea cada puerto de entrada con su implementación (caso de uso),
 * inyectando los puertos de salida correspondientes.
 */
@Configuration
public class HexagonalConfig {

    @Bean
    public ConsultarHorariosUseCase consultarHorariosUseCase(
            CitaRepositoryPort repo,
            DisponibilidadServicePort disponibilidadPort) {
        return new ConsultarHorariosService(repo, disponibilidadPort);
    }

    @Bean
    public AgendarCitaUseCase agendarCitaUseCase(
            CitaRepositoryPort repo,
            PacienteServicePort pacientePort,
            EspecialistaServicePort especialistaPort,
            ConsultarHorariosUseCase horariosUseCase) {
        return new AgendarCitaService(repo, pacientePort, especialistaPort, horariosUseCase);
    }

    @Bean
    public CancelarCitaUseCase cancelarCitaUseCase(CitaRepositoryPort repo) {
        return new CancelarCitaService(repo);
    }

    @Bean
    public ReagendarCitaUseCase reagendarCitaUseCase(
            CitaRepositoryPort repo,
            ConsultarHorariosUseCase horariosUseCase) {
        return new ReagendarCitaService(repo, horariosUseCase);
    }

    @Bean
    public CambiarEstadoCitaUseCase cambiarEstadoCitaUseCase(CitaRepositoryPort repo) {
        return new CambiarEstadoCitaService(repo);
    }

    @Bean
    public ConsultarCitaUseCase consultarCitaUseCase(CitaRepositoryPort repo) {
        return new ConsultarCitaService(repo);
    }
}
