package co.unicauca.auth_service.repository;

import co.unicauca.auth_service.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Juan Martin
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{
}
