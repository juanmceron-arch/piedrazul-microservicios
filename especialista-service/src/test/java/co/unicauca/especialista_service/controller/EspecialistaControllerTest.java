package co.unicauca.especialista_service.controller;

import co.unicauca.especialista_service.service.EspecialistaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 *
 * @author Juan Martin
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(EspecialistaController.class)
public class EspecialistaControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EspecialistaService service;

    @Test
    void debeCrearEspecialista() throws Exception {
        String json = """
        {
            "id":"esp1",
            "nombre":"Juan",
            "especialidad":"FISIOTERAPIA"
        }
        """;

        mockMvc.perform(post("/especialistas")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }
}
