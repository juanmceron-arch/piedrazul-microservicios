package co.unicauca.auth_service.repository;

import co.unicauca.auth_service.model.Usuario;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{
    @Query(value = """
            select *
            from usuario
            where rol = 'PACIENTE'
              and cast(id as text) like :documento || '%'
            order by id
            limit 10
            """, nativeQuery = true)
    List<Usuario> buscarPacientesPorPrefijoDocumento(@Param("documento") String documento);
}
