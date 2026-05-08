package co.unicauca.especialista_service.controller;

import co.unicauca.especialista_service.DTO.CrearEspecialistaDto;
import co.unicauca.especialista_service.model.TipoEspecialista;
import co.unicauca.especialista_service.model.Especialista;
import co.unicauca.especialista_service.service.EspecialistaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 *
 * @author Juan Martin
 */
@RestController
@RequestMapping({"/especialistas", "/api/especialistas"})
public class EspecialistaController {
    private final EspecialistaService service;

    public EspecialistaController(EspecialistaService service) {
        this.service = service;
    }
    
    @PostMapping
    public String crearEspecialista(@RequestBody CrearEspecialistaDto dto) {

        Especialista especialista = new Especialista(
                dto.getId(),
                dto.getNombre(),
                TipoEspecialista.valueOf(dto.getEspecialidad())
        );

        service.crearEspecialista(especialista);

        return "Especialista creado";
    }
    
    @GetMapping
    public List<Especialista> listar() {
        return service.listarEspecialistas();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Especialista> buscar(@PathVariable String id) {
        Especialista especialista = service.buscarEspecialista(id);
        if (especialista == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(especialista);
    }
    
    
}
