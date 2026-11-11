package com.tporopa.backend.service;

import com.tporopa.backend.model.Usuario;
import com.tporopa.backend.repository.SuscripcionRepository;
import com.tporopa.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests del servicio de suscripciones/Mercado Pago (P5).
 */
@ExtendWith(MockitoExtension.class)
class SuscripcionServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SuscripcionRepository suscripcionRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private SuscripcionService suscripcionService;

    @Test
    void crearPagoPremiumDevuelveUrlDePago() {
        Usuario u = new Usuario("Juan", "juan@test.com");
        u.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(u));
        when(suscripcionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String url = suscripcionService.crearPagoPremium(1L);

        assertNotNull(url);
        assertTrue(url.contains("mercadopago.com"));
    }

    @Test
    void confirmarPagoAprobadoMarcaUsuarioPremium() {
        Usuario u = new Usuario("Juan", "juan@test.com");
        u.setId(1L);
        u.setPremium(false);

        com.tporopa.backend.model.Suscripcion s = new com.tporopa.backend.model.Suscripcion(u, "PENDIENTE", "MP-1");
        s.setId(10L);

        when(suscripcionRepository.findByIdPago("MP-1")).thenReturn(java.util.Optional.of(s));
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(suscripcionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        suscripcionService.confirmarPago("MP-1", true);

        assertTrue(u.getPremium());
        verify(eventPublisher, times(1)).publicarSuscripcionActivada(1L, "MP-1");
    }
}
