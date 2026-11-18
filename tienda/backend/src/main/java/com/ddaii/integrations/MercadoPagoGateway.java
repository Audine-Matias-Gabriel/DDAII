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
import com.mercadopago.net.MPResultsResourcesPage;
import com.mercadopago.net.MPSearchRequest;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    /** Precio que se cobra por el plan premium (mp.premium-precio), en ARS. */
    public BigDecimal precioPremium() {
        return precioPremium;
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

        PreferenceRequest.PreferenceRequestBuilder builder = PreferenceRequest.builder()
                .items(List.of(item))
                .externalReference(String.valueOf(suscripcionId))
                .backUrls(PreferenceBackUrlsRequest.builder()
                        .success(frontendUrl + "/pago/exito")
                        .failure(frontendUrl + "/pago/error")
                        .pending(frontendUrl + "/pago/error")
                        .build());
        // auto_return (volver solo al sitio al aprobarse) Mercado Pago lo rechaza
        // con URLs localhost ("invalid_auto_return"). En local el pago se detecta
        // igual con buscarPagoAprobado (POST /api/suscripciones/verificar).
        if (usaAutoReturn()) {
            builder.autoReturn("approved");
        }
        PreferenceRequest request = builder.build();

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

    /**
     * Busca en Mercado Pago un pago aprobado de la suscripción (por
     * {@code external_reference}). Sirve cuando el comprador no vuelve por la
     * back URL: en localhost Mercado Pago no redirige solo.
     */
    public Optional<PagoConsultado> buscarPagoAprobado(Long suscripcionId) {
        exigirAccessToken();
        String referencia = String.valueOf(suscripcionId);
        MPSearchRequest busqueda = MPSearchRequest.builder()
                .limit(10)
                .offset(0)
                .filters(Map.of("external_reference", referencia))
                .build();
        try {
            MPResultsResourcesPage<Payment> pagina = new PaymentClient().search(busqueda, opciones());
            List<Payment> pagos = pagina.getResults() != null ? pagina.getResults() : List.of();
            return pagos.stream()
                    .filter(p -> "approved".equals(p.getStatus()))
                    .filter(p -> referencia.equals(p.getExternalReference()))
                    .findFirst()
                    .map(p -> new PagoConsultado(String.valueOf(p.getId()), p.getStatus(),
                            p.getExternalReference()));
        } catch (MPApiException e) {
            String detalle = e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage();
            throw new MercadoPagoException(
                    "No se pudieron buscar los pagos de la suscripción " + suscripcionId + ": " + detalle, e);
        } catch (MPException e) {
            throw new MercadoPagoException(
                    "No se pudo conectar con Mercado Pago: " + e.getMessage(), e);
        }
    }

    /** Mercado Pago solo acepta auto_return con back URLs públicas (no localhost). */
    boolean usaAutoReturn() {
        return frontendUrl != null && frontendUrl.startsWith("https://")
                && !frontendUrl.contains("localhost") && !frontendUrl.contains("127.0.0.1");
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
