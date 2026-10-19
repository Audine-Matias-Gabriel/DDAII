
package com.ddaii.services;

import com.ddaii.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ConsumidorPrueba {

    @RabbitListener(queues = RabbitMQConfig.COLA_PRUEBA)
    public void recibirMensaje(String mensaje) {
        System.out.println(
                "Mensaje recibido desde RabbitMQ: " + mensaje
        );
    }
}
