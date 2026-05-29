package co.unicauca.appointment_service.client;

import java.util.Map;

/**
 * Sujeto del patron Proxy (estructural).
 *
 * Define la operacion comun que comparten el objeto real
 * ({@link EspecialistaClient}) y su representante
 * ({@link EspecialistaClientProxy}), de forma que ambos sean
 * intercambiables para los servicios que los consumen.
 *
 * @author Juan Martin
 */
public interface EspecialistaGateway {

    Map<String, Object> obtenerEspecialista(String id);
}
