package co.unicauca.auth_service.service;

import co.unicauca.auth_service.DTO.RegisterRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class KeycloakAdminService {

    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin-client-id}")
    private String adminClientId;

    @Value("${keycloak.admin-client-secret}")
    private String adminClientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public void crearUsuario(RegisterRequest request) {
        String adminToken = obtenerTokenAdmin();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String username = String.valueOf(request.getId());
        String rol = normalizarRol(request.getRol());

        Map<String, Object> userBody = Map.of(
            "username", username,
            "enabled", true,
            "firstName", valorSeguro(request.getNombre()),
            "lastName", valorSeguro(request.getApellido()),
            "email", correoSeguro(request.getCorreo(), username),
            "emailVerified", true,
            "requiredActions", List.of(),
            "credentials", List.of(
                Map.of(
                    "type", "password",
                    "value", request.getPasswordHash(),
                    "temporary", false
                )
            ),
            "attributes", Map.of(
                "documento", List.of(username),
                "telefono", List.of(valorSeguro(request.getTelefono()))
            )
        );

        ResponseEntity<Void> response = restTemplate.exchange(
            keycloakServerUrl + "/admin/realms/" + realm + "/users",
            HttpMethod.POST,
            new HttpEntity<>(userBody, headers),
            Void.class
        );

        URI location = response.getHeaders().getLocation();

        if (location == null) {
            throw new RuntimeException("Keycloak no devolvió la ubicación del usuario creado");
        }

        String userId = extraerUserId(location);
        asignarRol(userId, rol, adminToken);
    }

    private String obtenerTokenAdmin() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", adminClientId);
        form.add("client_secret", adminClientSecret);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token",
            new HttpEntity<>(form, headers),
            Map.class
        );

        Object token = response.getBody().get("access_token");

        if (token == null) {
            throw new RuntimeException("No se pudo obtener token administrativo de Keycloak");
        }

        return token.toString();
    }

    private void asignarRol(String userId, String rol, String adminToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> roleResponse = restTemplate.exchange(
            keycloakServerUrl + "/admin/realms/" + realm + "/roles/" + rol,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Map.class
        );

        Map roleRepresentation = roleResponse.getBody();

        restTemplate.exchange(
            keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm",
            HttpMethod.POST,
            new HttpEntity<>(List.of(roleRepresentation), headers),
            Void.class
        );
    }

    private String extraerUserId(URI location) {
        String path = location.getPath();
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private String normalizarRol(String rol) {
        if (rol == null || rol.isBlank()) {
            return "PACIENTE";
        }

        return rol.trim().toUpperCase();
    }

    private String valorSeguro(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String correoSeguro(String correo, String username) {
        if (correo == null || correo.isBlank()) {
            return "usuario-" + username + "@piedrazul.local";
        }

        return correo.trim();
    }
}
