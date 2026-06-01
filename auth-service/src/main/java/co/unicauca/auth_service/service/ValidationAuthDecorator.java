package co.unicauca.auth_service.service;

import co.unicauca.auth_service.DTO.AuthResponse;
import co.unicauca.auth_service.DTO.RegisterRequest;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 *
 * @author Juan Martin
 */
public class ValidationAuthDecorator extends AuthDecorator {
    public ValidationAuthDecorator(AuthService authService) {
        super(authService);
    }

    @Override
    public AuthResponse registrar(RegisterRequest request) {

        if (request == null) {
            return new AuthResponse("Solicitud invalida", 0, null, false);
        }

        if (request.getId() <= 0) {
            return new AuthResponse("ID invalido", 0, null, false);
        }

        if (request.getPasswordHash() == null || request.getPasswordHash().isBlank()) {
            return new AuthResponse("Contrasena requerida", 0, null, false);
        }

        if (request.getPasswordHash().length() < 6) {
            return new AuthResponse("La contrasena debe tener minimo 6 caracteres", 0, null, false);
        }

        if (request.getRol() == null || request.getRol().isBlank()) {
            return new AuthResponse("Rol requerido", 0, null, false);
        }

        request.setNombre(normalizarNombre(request.getNombre()));
        request.setApellido(normalizarNombre(request.getApellido()));

        if (!nombreValido(request.getNombre())) {
            return new AuthResponse("Nombre invalido. Use solo letras y espacios", 0, null, false);
        }

        if (!nombreValido(request.getApellido())) {
            return new AuthResponse("Apellido invalido. Use solo letras y espacios", 0, null, false);
        }

        if (request.getCorreo() != null && !request.getCorreo().isBlank()
                && !request.getCorreo().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return new AuthResponse("Correo invalido", 0, null, false);
        }

        if (request.getTelefono() != null && !request.getTelefono().isBlank()
                && !request.getTelefono().matches("\\d{10}")) {
            return new AuthResponse("Telefono invalido. Use exactamente 10 numeros", 0, null, false);
        }

        if (request.getFechaNacimiento() != null && !request.getFechaNacimiento().isBlank()) {
            try {
                if (LocalDate.parse(request.getFechaNacimiento()).isAfter(LocalDate.now())) {
                    return new AuthResponse("La fecha de nacimiento no puede ser futura", 0, null, false);
                }
            } catch (DateTimeParseException ex) {
                return new AuthResponse("Fecha de nacimiento invalida", 0, null, false);
            }
        }

        return super.registrar(request);
    }

    private String normalizarNombre(String valor) {
        if (valor == null) {
            return "";
        }

        String sinTildes = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String limpio = sinTildes.trim().replaceAll("\\s+", " ").toLowerCase();

        if (limpio.isBlank()) {
            return limpio;
        }

        String[] partes = limpio.split(" ");
        StringBuilder resultado = new StringBuilder();

        for (String parte : partes) {
            if (resultado.length() > 0) {
                resultado.append(" ");
            }
            resultado.append(Character.toUpperCase(parte.charAt(0)));
            if (parte.length() > 1) {
                resultado.append(parte.substring(1));
            }
        }

        return resultado.toString();
    }

    private boolean nombreValido(String valor) {
        return valor != null && valor.matches("^[A-Za-z ]{2,60}$");
    }
}
