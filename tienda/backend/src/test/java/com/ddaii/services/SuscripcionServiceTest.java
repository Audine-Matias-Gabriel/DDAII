package com.ddaii.services;

import com.ddaii.domain.Usuario;
import com.ddaii.dto.SuscripcionResponse;
import com.ddaii.exceptions.MercadoPagoException;
import com.ddaii.integrations.MercadoPagoGateway;
import com.ddaii.integrations.MercadoPagoGateway.PreferenciaPago;
import com.ddaii.messaging.EventPublisher;
import com.ddaii.repositories.SuscripcionRepository;
import com.ddaii.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests del servicio de suscripciones/Mercado Pago (docs/PLAN-INTEGRACION.md 2.7-2.9).
 */
@ExtendWith(MockitoExtension.class)
class SuscripcionServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SuscripcionRepository suscripcionRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private MercadoPagoGateway mercadoPagoGateway;

    @InjectMocks
    private SuscripcionService suscripcionService;

    @Test
    void crearPagoPremiumDevuelveUrlDePagoYGuardaLaPreferencia() {
        Usuario u = new Usuario("Juan", "juan@test.com", "hash");
        u.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(u));
        when(suscripcionRepository.save(any())).thenAnswer(inv -> {
            com.ddaii.domain.Suscripcion s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });
        when(mercadoPagoGateway.crearPreferenciaPremium(10L)).thenReturn(
                new PreferenciaPago("pref-123", "https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=pref-123"));

        SuscripcionResponse respuesta = suscripcionService.crearPagoPremium(1L, "PREMIUM");

        assertEquals(10L, respuesta.getSuscripcionId());
        assertEquals("PENDIENTE", respuesta.getEstado());
        assertEquals("https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=pref-123",
                respuesta.getUrlPago());

        ArgumentCaptor<com.ddaii.domain.Suscripcion> guardada =
                ArgumentCaptor.forClass(com.ddaii.domain.Suscripcion.class);
        verify(suscripcionRepository, times(2)).save(guardada.capture());
        assertEquals("pref-123", guardada.getValue().getPreferenciaId());
        assertNull(guardada.getValue().getIdPago());
    }

    @Test
    void crearPagoPremiumSiMercadoPagoFallaPropagaElErrorYNoPublicaNada() {
        Usuario u = new Usuario("Juan", "juan@test.com", "hash");
        u.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(u));
        when(suscripcionRepository.save(any())).thenAnswer(inv -> {
            com.ddaii.domain.Suscripcion s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });
        when(mercadoPagoGateway.crearPreferenciaPremium(10L))
                .thenThrow(new MercadoPagoException("Mercado Pago caído"));

        assertThrows(MercadoPagoException.class,
                () -> suscripcionService.crearPagoPremium(1L, "PREMIUM"));

        verify(eventPublisher, never()).publicarSuscripcionActivada(any(), any(), any());
    }

    @Test
    void confirmarPagoAprobadoPublicaEventoSinMarcarPremiumDirecto() {
        Usuario u = new Usuario("Juan", "juan@test.com", "hash");
        u.setId(1L);
        u.setPremium(false);

        com.ddaii.domain.Suscripcion s = new com.ddaii.domain.Suscripcion(u, "PENDIENTE", "MP-1");
        s.setId(10L);

        when(suscripcionRepository.findByIdPago("MP-1")).thenReturn(java.util.Optional.of(s));
        when(suscripcionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        suscripcionService.confirmarPago("MP-1", true);

        assertEquals("APROBADA", s.getEstado());
        assertFalse(u.getPremium());
        verify(eventPublisher, times(1)).publicarSuscripcionActivada(1L, 10L, "MP-1");
    }

    @Test
    void confirmarPagoDuplicadoNoRepublicaEvento() {
        Usuario u = new Usuario("Juan", "juan@test.com", "hash");
        u.setId(1L);

        com.ddaii.domain.Suscripcion s = new com.ddaii.domain.Suscripcion(u, "APROBADA", "MP-1");
        s.setId(10L);

        when(suscripcionRepository.findByIdPago("MP-1")).thenReturn(java.util.Optional.of(s));

        suscripcionService.confirmarPago("MP-1", true);

        verify(eventPublisher, never()).publicarSuscripcionActivada(any(), any(), any());
    }

    @Test
    void marcarPremiumActivaUsuarioDeFormaIdempotente() {
        Usuario u = new Usuario("Juan", "juan@test.com", "hash");
        u.setId(1L);
        u.setPremium(false);

        when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(u));
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        suscripcionService.marcarPremium(1L);

        assertTrue(u.getPremium());
        verify(usuarioRepository, times(1)).save(u);
    }
}
