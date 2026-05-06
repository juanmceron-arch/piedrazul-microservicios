package co.unicauca.auth_service.factory;

import co.unicauca.auth_service.DTO.RegisterRequest;
import co.unicauca.auth_service.model.TipoGenero;
import co.unicauca.auth_service.model.TipoUsuario;
import co.unicauca.auth_service.model.Usuario;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

/**
 *
 * @author Juan Martin
 */
@Component
public class PacienteFactory implements UsuarioFactory{

    @Override
    public Usuario crearUsuario(RegisterRequest request) {
        Usuario usuario = new Usuario();
        
        usuario.setId(request.getId());
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setPasswordHash(request.getPasswordHash());
        usuario.setRol(TipoUsuario.PACIENTE);
        usuario.setTelefono(request.getTelefono());
        usuario.setCorreo(request.getCorreo());
        
        if (request.getGenero() != null){
            usuario.setGenero(TipoGenero.valueOf(request.getGenero().toUpperCase()));
        }
        
        if (request.getFechaNacimiento() != null) {
            usuario.setFechaNacimiento(LocalDate.parse(request.getFechaNacimiento()));
        }
        
        return usuario;
    }
    
}
