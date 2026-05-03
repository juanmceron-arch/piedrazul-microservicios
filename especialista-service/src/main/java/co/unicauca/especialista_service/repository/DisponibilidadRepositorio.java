package co.unicauca.especialista_service.repository;

import co.unicauca.especialista_service.model.DisponibilidadEspecialista;

/**
 *
 * @author Juan Martin
 */
public interface DisponibilidadRepositorio {
    void guardar(String id_especialista, DisponibilidadEspecialista disponibilidad);
    
    DisponibilidadEspecialista buscarPorEspecialistaId (String id_especialista);
}




