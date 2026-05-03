package co.unicauca.especialista_service.repository;

import java.util.List;
import co.unicauca.especialista_service.model.Especialista;

/**
 *
 * @author Juan Martin
 */
public interface EspecialistaRepositorio {
    Especialista buscarPorId(String id);
    List<Especialista> listarEspecialistas();
    void guardar(Especialista especialista);
}
