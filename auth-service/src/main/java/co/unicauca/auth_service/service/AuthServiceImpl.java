package co.unicauca.auth_service.service;

import co.unicauca.auth_service.DTO.AuthResponse;
import co.unicauca.auth_service.DTO.LoginRequest;
import co.unicauca.auth_service.DTO.RegisterRequest;
import co.unicauca.auth_service.factory.FactoryProducer;
import co.unicauca.auth_service.factory.UsuarioFactory;
import co.unicauca.auth_service.model.Usuario;
import co.unicauca.auth_service.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 *
 * @author Juan Martin
 */
@Service
public class AuthServiceImpl implements AuthService{
    private final UsuarioRepository repo;
    private final FactoryProducer factoryProducer;
    private final BCryptPasswordEncoder encoder;

    public AuthServiceImpl(UsuarioRepository repo, FactoryProducer factory, BCryptPasswordEncoder encoder) {
        this.repo = repo;
        this.factoryProducer = factory;
        this.encoder = encoder;
    }

    @Override
    public AuthResponse registrar(RegisterRequest request) {
        
        if (repo.existsById(request.getId())) {
            return new AuthResponse("Ya existe un usuario con ese documento",request.getId(),null,false);
        }
        
        UsuarioFactory factory = factoryProducer.getFactory(request.getRol());

        Usuario usuario = factory.crearUsuario(request);

        usuario.setPasswordHash(encoder.encode(usuario.getPasswordHash()));

        Usuario guardado = repo.save(usuario);

        return new AuthResponse("Usuario registrado",guardado.getId(),guardado.getRol().name(),true);
}

    @Override
    public AuthResponse login(LoginRequest request) {

        return repo.findById(request.getId())
                .map(usuario -> {

                    boolean ok = encoder.matches(request.getPasswordHash(),usuario.getPasswordHash());

                    if (ok) {
                        return new AuthResponse("Login exitoso",usuario.getId(),usuario.getRol().name(),true);
                    }

                    return new AuthResponse("Credenciales inválidas",0,null,false);
                })
                .orElse(new AuthResponse("Usuario no encontrado",0,null,false));
    }
    
    public Usuario obtenerPaciente(Integer id) {

        Usuario usuario = repo.findById(id).orElseThrow(() ->new RuntimeException("Usuario no encontrado"));

        if (!usuario.getRol().name().equals("PACIENTE")) {
            throw new RuntimeException("El usuario no es paciente");
        }

        return usuario;
    }
}
