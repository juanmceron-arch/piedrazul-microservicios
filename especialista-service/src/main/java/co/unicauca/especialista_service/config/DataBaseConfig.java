package co.unicauca.especialista_service.config;

import co.unicauca.especialista_service.conexion.SQLConexionBD;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
public class DataBaseConfig {

    @Bean
    public Connection connection() throws SQLException {
        SQLConexionBD.inicializar();
        return SQLConexionBD.SQLiteConexionBD();
    }
}

