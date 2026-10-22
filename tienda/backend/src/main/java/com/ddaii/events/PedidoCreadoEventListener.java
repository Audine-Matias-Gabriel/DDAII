package com.ddaii.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PedidoCreadoEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoCreadoEventListener.class);

    private final PedidoCreadoPublisher publisher;

    public PedidoCreadoEventListener(
            PedidoCreadoPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Publica el pedido en RabbitMQ una vez confirmada la transacción.
     *
     * El fallo de la publicación no se propaga a propósito: este callback
     * corre dentro del {@code commit()} y una excepción acá cancelaría la
     * respuesta del POST /api/pedidos con un 500, aunque el pedido ya esté
     * guardado y el stock ya descontado. El frontend no vacía el carrito ante
     * un error, así que el usuario reintentaría y se crearía un pedido
     * duplicado. Se registra el error y se sigue.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alConfirmarPedido(PedidoCreadoEvent evento) {
        try {
            publisher.publicar(evento);
        } catch (RuntimeException e) {
            log.error(
                    "No se pudo publicar el pedido {} en RabbitMQ: {}",
                    evento.pedidoId(),
                    e.getMessage(),
                    e
            );
        }
    }
}
