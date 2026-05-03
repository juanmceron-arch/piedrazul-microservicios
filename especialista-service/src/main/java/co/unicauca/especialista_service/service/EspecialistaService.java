package co.unicauca.especialista_service.service;

import java.util.List;
import co.unicauca.especialista_service.model.Especialista;
import co.unicauca.especialista_service.repository.EspecialistaRepositorio;
import org.springframework.stereotype.Service;

/**
 *
 * @author Juan Martin
 */
@Service
public class EspecialistaService {
    private final EspecialistaRepositorio repositorio;

    public EspecialistaService(EspecialistaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public void crearEspecialista(Especialista especialista) {

        if (especialista == null) {
            throw new IllegalArgumentException("El especialista no puede ser null");
        }

        if (especialista.getId() == null || especialista.getId().isEmpty()) {
            throw new IllegalArgumentException("ID inválido");
        }

        if (especialista.getNombre() == null || especialista.getNombre().isEmpty()) {
            throw new IllegalArgumentException("Nombre inválido");
        }

        repositorio.guardar(especialista);
    }

    public Especialista buscarEspecialista(String id) {
        return repositorio.buscarPorId(id);
    }

    public List<Especialista> listarEspecialistas() {
        return repositorio.listarEspecialistas();
    }
}
