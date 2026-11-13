package com.ddaii.integrations;

import com.ddaii.exceptions.MercadoPagoException;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Único punto de contacto con la API de Mercado Pago (patrón Facade): el resto
 * del backend no conoce el SDK. Usa Checkout Pro: se crea una preferencia de
 * pago único por el plan premium y el comprador paga en la página de Mercado
 * Pago.
 */
@Component
public class MercadoPagoGateway {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoGateway.class);

    private final String accessToken;
    private final BigDecimal precioPremium;
    private final String frontendUrl;

    public MercadoPagoGateway(
            @Value("${mp.access-token:}") String accessToken,
            @Value("${mp.premium-precio:5000}") BigDecimal precioPremium,
            @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.accessToken = accessToken;
        this.precioPremium = precioPremium;
        this.frontendUrl = frontendUrl;
    }

    /** Pago consultado: su estado en Mercado Pago y la suscripción a la que pertenece. */
    public record PagoConsultado(String idPago, String estado, String suscripcionRef) {}

    /** Preferencia creada: su id y la URL donde el comprador paga. */
    public record PreferenciaPago(String preferenciaId, String urlPago) {}

    /**
     * Crea la preferencia de pago del plan premium. El id de la suscripción
     * viaja como {@code external_reference}: es lo que después permite
     * relacionar el pago con la suscripción.
     */
    public PreferenciaPago crearPreferenciaPremium(Long suscripcionId) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new MercadoPagoException(
                    "Mercado Pago no está configurado: falta la variable MP_ACCESS_TOKEN");
        }

        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .title("Plan Premium - Web de Ropa")
                .quantity(1)
                .currencyId("ARS")
                .unitPrice(precioPremium)
                .build();

        // Sin auto_return: Mercado Pago lo exige con URLs HTTPS y el frontend
        // corre en localhost. El comprador vuelve con "Volver al sitio".
        PreferenceRequest request = PreferenceRequest.builder()
                .items(List.of(item))
                .externalReference(String.valueOf(suscripcionId))
                .backUrls(PreferenceBackUrlsRequest.builder()
                        .success(frontendUrl + "/pago/exito")
                        .failure(frontendUrl + "/pago/error")
                        .pending(frontendUrl + "/pago/error")
                        .build())
                .build();

        MPRequestOptions opciones = MPRequestOptions.builder()
                .accessToken(accessToken)
                .build();

        try {
            Preference preferencia = new PreferenceClient().create(request, opciones);
            log.info("Preferencia de pago creada. suscripcionId={} preferenciaId={}",
                    suscripcionId, preferencia.getId());
            return new PreferenciaPago(preferencia.getId(), preferencia.getInitPoint());
        } catch (MPApiException e) {
            String detalle = e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage();
            throw new MercadoPagoException(
                    "Mercado Pago rechazó la creación del pago: " + detalle, e);
        } catch (MPException e) {
            throw new MercadoPagoException(
                    "No se pudo conectar con Mercado Pago: " + e.getMessage(), e);
        }
    }

    /**
     * Consulta un pago a Mercado Pago. Se consulta siempre a la API (no se
     * confía en lo que llega por la URL de retorno, que el comprador podría
     * alterar).
     */
    public PagoConsultado consultarPago(Long paymentId) {
        exigirAccessToken();
        try {
            Payment pago = new PaymentClient().get(paymentId, opciones());
            return new PagoConsultado(String.valueOf(pago.getId()), pago.getStatus(),
                    pago.getExternalReference());
        } catch (MPApiException e) {
            String detalle = e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage();
            throw new MercadoPagoException("No se pudo consultar el pago " + paymentId + ": " + detalle, e);
        } catch (MPException e) {
            throw new MercadoPagoException(
                    "No se pudo conectar con Mercado Pago: " + e.getMessage(), e);
        }
    }

    private MPRequestOptions opciones() {
        return MPRequestOptions.builder().accessToken(accessToken).build();
    }

    private void exigirAccessToken() {
        if (accessToken == null || accessToken.isBlank()) {
            throw new MercadoPagoException(
                    "Mercado Pago no está configurado: falta la variable MP_ACCESS_TOKEN");
        }
    }
}
