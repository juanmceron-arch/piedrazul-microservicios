package com.piedrazul.citas.cliente;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.util.Map;

@Component
public class EspecialistaCliente {

    private final RestClient restClient;

    public EspecialistaCliente() {
        this.restClient = RestClient.builder()
            .baseUrl("http://localhost:8083")
            .build();
    }

    public boolean existeEspecialista(String especialistaId) {
        try {
            restClient.get()
                .uri("/api/especialistas/" + especialistaId)
                .retrieve()
                .body(Map.class);
            return true;
        } catch (RestClientException e) {
            return false;
        }
    }
}