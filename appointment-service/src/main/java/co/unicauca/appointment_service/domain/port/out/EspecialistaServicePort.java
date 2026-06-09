package co.unicauca.appointment_service.domain.port.out;

/** Puerto de salida: obtener datos del servicio de especialistas. */
public interface EspecialistaServicePort {

    record DatosEspecialista(String id, String nombre, String especialidad) {}

    DatosEspecialista obtener(String especialistaId);
}
