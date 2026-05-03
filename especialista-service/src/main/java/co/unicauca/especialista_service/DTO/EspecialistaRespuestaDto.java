package co.unicauca.especialista_service.DTO;

/**
 *
 * @author Juan Martin
 */
public class EspecialistaRespuestaDto {
    private String id;
    private String nombre;
    private String especialidad;

    public EspecialistaRespuestaDto() {
    }

    public EspecialistaRespuestaDto(String id, String nombre, String especialidad) {
        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEspecialidad() {
        return especialidad;
    }
        
}
