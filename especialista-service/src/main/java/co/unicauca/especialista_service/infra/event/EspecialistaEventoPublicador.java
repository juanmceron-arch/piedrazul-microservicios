package co.unicauca.especialista_service.infra.event;

import co.unicauca.especialista_service.infra.config.PiedraAzulRabbitConfig;
import co.unicauca.especialista_service.model.Especialista;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "piedrazul.async", name = "enabled", havingValue = "true")
public class EspecialistaEventoPublicador {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public EspecialistaEventoPublicador(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publicarEspecialistaCreado(Especialista especialista) {
        try {
            Map<String, String> evento = Map.of(
                    "tipo", "ESPECIALISTA_CREADO",
                    "id", especialista.getId(),
                    "nombre", especialista.getNombre(),
                    "especialidad", especialista.getEspecialidad().name()
            );

            String payload = objectMapper.writeValueAsString(evento);
            rabbitTemplate.convertAndSend(
                    PiedraAzulRabbitConfig.ESPECIALISTA_EXCHANGE,
                    PiedraAzulRabbitConfig.ESPECIALISTA_CREADO_ROUTING_KEY,
                    payload
            );
        } catch (Exception ex) {
            System.out.println("Aviso: no se pudo publicar el evento asincrono de especialista: " + ex.getMessage());
        }
    }
}
