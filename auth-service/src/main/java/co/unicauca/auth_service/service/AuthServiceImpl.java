package co.unicauca.auth_service.service;

import co.unicauca.auth_service.DTO.AuthResponse;
import co.unicauca.auth_service.DTO.LoginRequest;
import co.unicauca.auth_service.DTO.PacienteResponse;
import co.unicauca.auth_service.DTO.RegisterRequest;
import co.unicauca.auth_service.factory.FactoryProducer;
import co.unicauca.auth_service.factory.UsuarioFactory;
import co.unicauca.auth_service.model.Usuario;
import co.unicauca.auth_service.repository.UsuarioRepository;
import java.text.Normalizer;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final UsuarioRepository repo;
    private final FactoryProducer factoryProducer;
    private final BCryptPasswordEncoder encoder;
    private final KeycloakAdminService keycloakAdminService;


    public AuthServiceImpl(UsuarioRepository repo, FactoryProducer factory, BCryptPasswordEncoder encoder, KeycloakAdminService keycloakAdminService) {
        this.repo = repo;
        this.factoryProducer = factory;
        this.encoder = encoder;
        this.keycloakAdminService = keycloakAdminService;
    }

    @Override
    public AuthResponse registrar(RegisterRequest request) {

        if (repo.existsById(request.getId())) {
            return new AuthResponse("Ya existe un usuario con ese documento", request.getId(), null, false);
        }

        UsuarioFactory factory = factoryProducer.getFactory(request.getRol());

        Usuario usuario = factory.crearUsuario(request);
        usuario.setNombre(normalizarNombre(usuario.getNombre()));
        usuario.setApellido(normalizarNombre(usuario.getApellido()));

        usuario.setPasswordHash(encoder.encode(usuario.getPasswordHash()));

        keycloakAdminService.crearUsuario(request);

        Usuario guardado = repo.save(usuario);

        return new AuthResponse(
                "Usuario registrado",
                guardado.getId(),
                guardado.getRol().name(),
                true,
                guardado.getNombre(),
                guardado.getApellido());
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        return repo.findById(request.getId())
                .map(usuario -> {

                    boolean ok = encoder.matches(request.getPasswordHash(), usuario.getPasswordHash());

                    if (ok) {
                        return new AuthResponse(
                                "Login exitoso",
                                usuario.getId(),
                                usuario.getRol().name(),
                                true,
                                usuario.getNombre(),
                                usuario.getApellido());
                    }

                    return new AuthResponse("Credenciales inválidas", 0, null, false);
                })
                .orElse(new AuthResponse("Usuario no encontrado", 0, null, false));
    }

    public PacienteResponse obtenerPaciente(Integer id) {

        Usuario usuario = repo.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuario.getRol().name().equals("PACIENTE")) {
            throw new RuntimeException("El usuario no es paciente");
        }

        return new PacienteResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getGenero(),
                usuario.getTelefono(),
                usuario.getFechaNacimiento(),
                usuario.getCorreo()
        );
    }

    @Override
    public List<PacienteResponse> buscarPacientes(String documento) {
        String prefijo = String.valueOf(documento == null ? "" : documento).replaceAll("\\D", "");

        if (prefijo.length() < 3) {
            return List.of();
        }

        return repo.buscarPacientesPorPrefijoDocumento(prefijo).stream()
                .map(this::toPacienteResponse)
                .toList();
    }

    private PacienteResponse toPacienteResponse(Usuario usuario) {
        return new PacienteResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getGenero(),
                usuario.getTelefono(),
                usuario.getFechaNacimiento(),
                usuario.getCorreo()
        );
    }

    private String normalizarNombre(String valor) {
        if (valor == null) {
            return null;
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
}
