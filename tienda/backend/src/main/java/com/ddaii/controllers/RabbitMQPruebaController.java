
package com.ddaii.controllers;

import com.ddaii.services.MensajePruebaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rabbitmq/prueba")
public class RabbitMQPruebaController {

    private final MensajePruebaService mensajePruebaService;

    public RabbitMQPruebaController(
            MensajePruebaService mensajePruebaService) {
        this.mensajePruebaService = mensajePruebaService;
    }

    @PostMapping
    public ResponseEntity<String> enviar(
            @RequestBody String mensaje) {

        mensajePruebaService.enviarMensaje(mensaje);

        return ResponseEntity.accepted()
                .body("Mensaje enviado a RabbitMQ");
    }
}
