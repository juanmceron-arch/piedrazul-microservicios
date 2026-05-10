package co.unicauca.auth_service.factory;

import org.springframework.stereotype.Component;

@Component
public class FactoryProducer {    
    public static UsuarioFactory getFactory(String rol){
        if ("PACIENTE".equalsIgnoreCase(rol)) {
            return new PacienteFactory();
        }
        
        if ("AGENDADOR".equalsIgnoreCase(rol)) {
            return new AgendadorFactory();
        }

        throw new IllegalArgumentException("Rol no valido");
    }
}
