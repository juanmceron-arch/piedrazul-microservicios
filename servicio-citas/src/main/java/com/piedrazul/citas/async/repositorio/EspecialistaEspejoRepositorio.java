package com.piedrazul.citas.async.repositorio;

import com.piedrazul.citas.async.modelo.EspecialistaEspejo;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class EspecialistaEspejoRepositorio {

    private final ConcurrentMap<String, EspecialistaEspejo> especialistas = new ConcurrentHashMap<>();

    public void guardar(EspecialistaEspejo especialista) {
        if (especialista != null && especialista.getId() != null && !especialista.getId().isBlank()) {
            especialistas.put(especialista.getId(), especialista);
        }
    }

    public boolean existe(String id) {
        return id != null && especialistas.containsKey(id);
    }

    public Optional<EspecialistaEspejo> buscarPorId(String id) {
        return Optional.ofNullable(especialistas.get(id));
    }

    public List<EspecialistaEspejo> listar() {
        return especialistas.values()
                .stream()
                .sorted(Comparator.comparing(EspecialistaEspejo::getNombre, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }
}
