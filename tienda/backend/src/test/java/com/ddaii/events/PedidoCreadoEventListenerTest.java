package com.ddaii.events;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;

import com.ddaii.domain.Genero;

@ExtendWith(MockitoExtension.class)
class PedidoCreadoEventListenerTest {

    @Mock
    private PedidoCreadoPublisher publisher;

    @InjectMocks
    private PedidoCreadoEventListener listener;

    private PedidoCreadoEvent evento() {
        return new PedidoCreadoEvent(
                7L,
                List.of(new PedidoCreadoEvent.ItemPedido(
                        1L,
                        "Remera Nike",
                        Genero.HOMBRE,
                        "M",
                        2
                ))
        );
    }

    @Test
    void alConfirmarPedido_publicaElEvento() {
        PedidoCreadoEvent evento = evento();

        listener.alConfirmarPedido(evento);

        verify(publisher).publicar(evento);
    }

    /**
     * Regresion del bug: el listener corre dentro del commit(), asi que si
     * relanza el error del broker el POST /api/pedidos responde 500 aunque el
     * pedido ya este guardado. El frontend no vacia el carrito ante un error,
     * con lo cual el reintento del usuario crearia un pedido duplicado.
     */
    @Test
    void alConfirmarPedido_siLaPublicacionFalla_noPropagaElError() {
        PedidoCreadoEvent evento = evento();

        doThrow(new AmqpException("broker caido"))
                .when(publisher)
                .publicar(evento);

        assertThatCode(() -> listener.alConfirmarPedido(evento))
                .doesNotThrowAnyException();
    }
}
