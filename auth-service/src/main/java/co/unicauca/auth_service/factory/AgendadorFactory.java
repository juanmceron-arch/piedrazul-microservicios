package co.unicauca.auth_service.factory;

import co.unicauca.auth_service.DTO.RegisterRequest;
import co.unicauca.auth_service.model.TipoUsuario;
import co.unicauca.auth_service.model.Usuario;
import org.springframework.stereotype.Component;

/**
 *
 * @author Juan Martin
 */
@Component
public class AgendadorFactory implements UsuarioFactory{

    @Override
    public Usuario crearUsuario(RegisterRequest request) {
        Usuario usuario = new Usuario();
        
        usuario.setId(request.getId());
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setPasswordHash(request.getPasswordHash());
        usuario.setRol(TipoUsuario.AGENDADOR);
        
        return usuario;
    }
    
}
