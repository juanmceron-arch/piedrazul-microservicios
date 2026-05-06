package com.piedrazul.citas.cliente;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.util.Map;

@Component
public class PacienteCliente {

    private final RestClient restClient;

    public PacienteCliente() {
        this.restClient = RestClient.builder()
            .baseUrl("http://localhost:8082")
            .build();
    }

    public boolean existePaciente(String pacienteId) {
        try {
            restClient.get()
                .uri("/api/pacientes/" + pacienteId)
                .retrieve()
                .body(Map.class);
            return true;
        } catch (RestClientException e) {
            return false;
        }
    }

    public void crearPaciente(Map<String, String> datos) {
        try {
            restClient.post()
                .uri("/api/pacientes")
                .body(datos)
                .retrieve()
                .body(Map.class);
        } catch (RestClientException e) {
            // Si falla la creación, lo ignoramos y seguimos
            System.out.println("Aviso: no se pudo registrar el paciente: " + e.getMessage());
        }
    }
}