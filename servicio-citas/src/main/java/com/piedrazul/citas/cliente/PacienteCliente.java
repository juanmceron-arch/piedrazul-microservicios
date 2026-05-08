package com.piedrazul.citas.cliente;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class PacienteCliente {

    private final RestClient restClient;

    public PacienteCliente(@Value("${piedrazul.servicios.pacientes-url:http://localhost:8082}") String pacientesUrl) {
        this.restClient = RestClient.builder()
            .baseUrl(pacientesUrl)
            .build();
    }

    public boolean existePaciente(String pacienteId) {
        if (pacienteId == null || pacienteId.isBlank()) {
            return false;
        }

        try {
            Map<?, ?> respuesta = restClient.get()
                .uri("/api/pacientes/{id}", pacienteId)
                .retrieve()
                .body(Map.class);
            return respuesta != null;
        } catch (RestClientException e) {
            return false;
        }
    }

    public boolean crearPaciente(Map<String, String> datos) {
        try {
            Map<?, ?> respuesta = restClient.post()
                .uri("/api/pacientes")
                .body(datos)
                .retrieve()
                .body(Map.class);
            return respuesta != null;
        } catch (RestClientException e) {
            return false;
        }
    }
}
