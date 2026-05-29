package co.unicauca.appointment_service.client;

import java.util.Map;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Proxy (patron estructural GoF).
 *
 * Representa y controla el acceso al objeto real {@link EspecialistaClient}
 * sin alterar su contrato. Actua como "smart reference": registra cada
 * acceso al servicio externo y delega la llamada al sujeto real,
 * conservando exactamente el mismo comportamiento observable.
 *
 * Se marca como @Primary para que sea la implementacion inyectada por
 * defecto donde se requiera un {@link EspecialistaGateway}.
 *
 * @author Juan Martin
 */
@Primary
@Component
public class EspecialistaClientProxy implements EspecialistaGateway {

    private final EspecialistaClient especialistaReal;

    public EspecialistaClientProxy(EspecialistaClient especialistaReal) {
        this.especialistaReal = especialistaReal;
    }

    @Override
    public Map<String, Object> obtenerEspecialista(String id) {
        System.out.println("[Proxy] Solicitando especialista id=" + id);

        Map<String, Object> resultado = especialistaReal.obtenerEspecialista(id);

        System.out.println("[Proxy] Especialista id=" + id + " resuelto");

        return resultado;
    }
}
