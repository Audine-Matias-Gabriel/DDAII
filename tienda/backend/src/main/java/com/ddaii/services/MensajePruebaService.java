
package com.ddaii.services;

import com.ddaii.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class MensajePruebaService {

    private final RabbitTemplate rabbitTemplate;

    public MensajePruebaService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enviarMensaje(String mensaje) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.COLA_PRUEBA,
                mensaje
        );
    }
}
