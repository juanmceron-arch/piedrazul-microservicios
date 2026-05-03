package co.unicauca.especialista_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Juan Martin
 */
@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "Especialista-service funcionando";
    }
}