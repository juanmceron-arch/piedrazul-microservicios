package co.unicauca.auth_service.factory;

import co.unicauca.auth_service.DTO.RegisterRequest;
import co.unicauca.auth_service.model.Usuario;

/**
 *
 * @author Juan Martin
 */
public interface UsuarioFactory {
    Usuario crearUsuario(RegisterRequest request);
}
