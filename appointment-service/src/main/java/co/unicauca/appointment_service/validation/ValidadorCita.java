package co.unicauca.appointment_service.validation;

/**
 * Manejador abstracto del patron Chain of Responsibility (comportamiento).
 *
 * Cada validador realiza su comprobacion y, si la supera, delega en el
 * siguiente eslabon de la cadena. Si una comprobacion falla, lanza la
 * excepcion correspondiente y la cadena se detiene.
 *
 * @author Juan Martin
 */
public abstract class ValidadorCita {

    private ValidadorCita siguiente;

    public ValidadorCita enlazarCon(ValidadorCita siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    /**
     * Plantilla de la cadena: valida en este eslabon y continua.
     */
    public final void validar(ContextoValidacion contexto) {
        validarEslabon(contexto);
        if (siguiente != null) {
            siguiente.validar(contexto);
        }
    }

    /**
     * Comprobacion concreta de cada validador.
     */
    protected abstract void validarEslabon(ContextoValidacion contexto);
}
