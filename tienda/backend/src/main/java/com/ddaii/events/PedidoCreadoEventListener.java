package com.ddaii.events;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PedidoCreadoEventListener {

    private final PedidoCreadoPublisher publisher;

    public PedidoCreadoEventListener(
            PedidoCreadoPublisher publisher) {
        this.publisher = publisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alConfirmarPedido(PedidoCreadoEvent evento) {
        publisher.publicar(evento);
    }
}