package co.unicauca.auth_service.config;

import co.unicauca.auth_service.service.AuthService;
import co.unicauca.auth_service.service.AuthServiceImpl;
import co.unicauca.auth_service.service.LoggingAuthDecorator;
import co.unicauca.auth_service.service.ValidationAuthDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Juan Martin
 */
@Configuration
public class AuthConfig {
    @Bean
    public AuthService authService(AuthServiceImpl baseService) {

        return new LoggingAuthDecorator(
                new ValidationAuthDecorator(baseService)
        );
    }
}
