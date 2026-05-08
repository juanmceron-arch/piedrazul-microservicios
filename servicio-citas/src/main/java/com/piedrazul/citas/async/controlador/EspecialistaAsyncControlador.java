package com.piedrazul.citas.async.controlador;

import com.piedrazul.citas.async.modelo.EspecialistaEspejo;
import com.piedrazul.citas.async.repositorio.EspecialistaEspejoRepositorio;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/citas/async/especialistas")
@ConditionalOnProperty(prefix = "piedrazul.async", name = "enabled", havingValue = "true")
public class EspecialistaAsyncControlador {

    private final EspecialistaEspejoRepositorio repositorio;

    public EspecialistaAsyncControlador(EspecialistaEspejoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @GetMapping
    public List<EspecialistaEspejo> listar() {
        return repositorio.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspecialistaEspejo> buscar(@PathVariable String id) {
        return repositorio.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
