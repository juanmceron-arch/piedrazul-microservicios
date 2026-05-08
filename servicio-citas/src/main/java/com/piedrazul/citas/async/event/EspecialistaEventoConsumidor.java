package com.piedrazul.citas.async.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.piedrazul.citas.async.config.PiedraAzulRabbitConfig;
import com.piedrazul.citas.async.modelo.EspecialistaEspejo;
import com.piedrazul.citas.async.repositorio.EspecialistaEspejoRepositorio;
import java.util.Map;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "piedrazul.async", name = "enabled", havingValue = "true")
public class EspecialistaEventoConsumidor {

    private final EspecialistaEspejoRepositorio repositorio;
    private final ObjectMapper objectMapper;

    public EspecialistaEventoConsumidor(EspecialistaEspejoRepositorio repositorio, ObjectMapper objectMapper) {
        this.repositorio = repositorio;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = PiedraAzulRabbitConfig.ESPECIALISTA_CREADO_QUEUE)
    public void recibirEspecialistaCreado(String payload) {
        try {
            Map<String, String> evento = objectMapper.readValue(payload, new TypeReference<Map<String, String>>() {});
            if (!"ESPECIALISTA_CREADO".equals(evento.get("tipo"))) {
                return;
            }

            EspecialistaEspejo especialista = new EspecialistaEspejo(
                    evento.get("id"),
                    evento.get("nombre"),
                    evento.get("especialidad")
            );
            repositorio.guardar(especialista);
            System.out.println("Recibido especialista asincrono: " + especialista.getId() + " - " + especialista.getNombre());
        } catch (Exception ex) {
            System.out.println("Aviso: no se pudo procesar el evento asincrono de especialista: " + ex.getMessage());
        }
    }
}
