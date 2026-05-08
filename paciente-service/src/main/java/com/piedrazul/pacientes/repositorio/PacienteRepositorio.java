package com.piedrazul.pacientes.repositorio;

import com.piedrazul.pacientes.modelo.Paciente;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class PacienteRepositorio {

    private final ConcurrentMap<String, Paciente> pacientes = new ConcurrentHashMap<>();

    public Paciente guardar(Paciente paciente) {
        pacientes.put(paciente.getId(), paciente);
        return paciente;
    }

    public Optional<Paciente> buscarPorId(String id) {
        return Optional.ofNullable(pacientes.get(id));
    }

    public boolean existe(String id) {
        return id != null && pacientes.containsKey(id);
    }

    public List<Paciente> listar() {
        return pacientes.values()
                .stream()
                .sorted(Comparator.comparing(Paciente::getNombre, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    public boolean eliminar(String id) {
        return pacientes.remove(id) != null;
    }
}
