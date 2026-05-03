
package co.unicauca.especialista_service.service;

import co.unicauca.especialista_service.model.Especialista;
import co.unicauca.especialista_service.model.TipoEspecialista;
import co.unicauca.especialista_service.repository.SQLEspecialistaRepositorio;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author Juan Martin
 */
public class EspecialistaServiceTest {
    private SQLEspecialistaRepositorio repositorio;
    private EspecialistaService service;

    @BeforeEach
    void setUp() {
        repositorio = mock(SQLEspecialistaRepositorio.class);
        service = new EspecialistaService(repositorio);
    }

    @Test
    void debeCrearEspecialista() {
        Especialista especialista = new Especialista(
                "esp1",
                "Juan",
                TipoEspecialista.FISIOTERAPIA
        );

        service.crearEspecialista(especialista);

        verify(repositorio, times(1)).guardar(especialista);
    }

    @Test
    void debeConsultarEspecialista() {
        Especialista especialista = new Especialista(
                "esp1",
                "Juan",
                TipoEspecialista.FISIOTERAPIA
        );

        when(repositorio.buscarPorId("esp1")).thenReturn(especialista);

        Especialista resultado = service.buscarEspecialista("esp1");

        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
    }
}
