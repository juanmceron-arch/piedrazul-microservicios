package com.piedrazul.citas.cliente;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class EspecialistaCliente {

    private final RestClient restClient;

    public EspecialistaCliente(@Value("${piedrazul.servicios.especialistas-url:http://localhost:8083}") String especialistasUrl) {
        this.restClient = RestClient.builder()
            .baseUrl(especialistasUrl)
            .build();
    }

    public boolean existeEspecialista(String especialistaId) {
        if (especialistaId == null || especialistaId.isBlank()) {
            return false;
        }

        try {
            Map<?, ?> respuesta = restClient.get()
                .uri("/api/especialistas/{id}", especialistaId)
                .retrieve()
                .body(Map.class);
            return respuesta != null;
        } catch (RestClientException e) {
            return false;
        }
    }
}
