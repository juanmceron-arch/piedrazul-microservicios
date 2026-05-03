package co.unicauca.especialista_service.model;

/**
 *
 * @author Juan Martin
 */
public class Especialista {
    private String id;
    private String nombre;
    private TipoEspecialista especialidad;
    private DisponibilidadEspecialista disponibilidad;

    public Especialista() {
    }

    public Especialista(String id, String nombre, TipoEspecialista especialidad, DisponibilidadEspecialista disponibilidad) {
        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.disponibilidad = disponibilidad;
    }
    
    public Especialista(String id, String nombre, TipoEspecialista especialidad) {
        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.disponibilidad = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoEspecialista getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(TipoEspecialista especialidad) {
        this.especialidad = especialidad;
    }

    public DisponibilidadEspecialista getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(DisponibilidadEspecialista disponibilidad) {
        this.disponibilidad = disponibilidad;
    }
    
    @Override
    public String toString() {
        return nombre + " (" + especialidad + ")";
    }
}
