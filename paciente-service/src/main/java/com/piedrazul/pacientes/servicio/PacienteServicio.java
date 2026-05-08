package com.piedrazul.pacientes.servicio;

import com.piedrazul.pacientes.modelo.Paciente;
import com.piedrazul.pacientes.repositorio.PacienteRepositorio;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PacienteServicio {

    private final PacienteRepositorio repositorio;

    public PacienteServicio(PacienteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Paciente guardar(Paciente paciente) {
        validar(paciente);
        paciente.setId(paciente.getId().trim());
        paciente.setNombre(normalizar(paciente.getNombre()));
        paciente.setApellido(normalizar(paciente.getApellido()));
        paciente.setTelefono(normalizar(paciente.getTelefono()));
        paciente.setGenero(normalizar(paciente.getGenero()));
        return repositorio.guardar(paciente);
    }

    public Optional<Paciente> buscarPorId(String id) {
        return repositorio.buscarPorId(id);
    }

    public List<Paciente> listar() {
        return repositorio.listar();
    }

    public boolean eliminar(String id) {
        return repositorio.eliminar(id);
    }

    private void validar(Paciente paciente) {
        if (paciente == null) {
            throw new IllegalArgumentException("El paciente es obligatorio");
        }
        if (estaVacio(paciente.getId())) {
            throw new IllegalArgumentException("El id del paciente es obligatorio");
        }
        if (estaVacio(paciente.getNombre())) {
            throw new IllegalArgumentException("El nombre del paciente es obligatorio");
        }
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
