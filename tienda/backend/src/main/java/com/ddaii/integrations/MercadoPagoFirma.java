package com.ddaii.integrations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Valida la firma de las notificaciones (webhooks) de Mercado Pago.
 *
 * Header {@code x-signature: ts=<timestamp>,v1=<hmac>}. El HMAC-SHA256 se calcula con la clave
 * secreta del webhook (MP_WEBHOOK_SECRET, en "Tus integraciones → Webhooks") sobre
 * {@code id:<data.id>;request-id:<x-request-id>;ts:<ts>;}.
 *
 * Sin MP_WEBHOOK_SECRET no se valida la firma, pero igual es seguro: el backend nunca confía en el
 * contenido de la notificación y siempre consulta el pago a la API de Mercado Pago.
 */
@Component
public class MercadoPagoFirma {

    private final String secreto;

    public MercadoPagoFirma(@Value("${mp.webhook-secret:}") String secreto) {
        this.secreto = secreto;
    }

    public boolean configurada() {
        return secreto != null && !secreto.isBlank();
    }

    /** true si la firma es válida, o si no hay clave configurada. */
    public boolean esValida(String xSignature, String xRequestId, String dataId) {
        if (!configurada()) return true;
        if (xSignature == null || dataId == null) return false;

        String ts = null;
        String v1 = null;
        for (String parte : xSignature.split(",")) {
            String[] kv = parte.trim().split("=", 2);
            if (kv.length != 2) continue;
            if (kv[0].equals("ts")) ts = kv[1];
            if (kv[0].equals("v1")) v1 = kv[1];
        }
        if (ts == null || v1 == null) return false;

        StringBuilder manifiesto = new StringBuilder("id:").append(dataId.toLowerCase()).append(';');
        if (xRequestId != null && !xRequestId.isBlank()) {
            manifiesto.append("request-id:").append(xRequestId).append(';');
        }
        manifiesto.append("ts:").append(ts).append(';');

        String esperado = hmacSha256Hex(secreto, manifiesto.toString());
        return MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8),
                v1.toLowerCase().getBytes(StandardCharsets.UTF_8));
    }

    static String hmacSha256Hex(String clave, String mensaje) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(clave.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(mensaje.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular el HMAC", e);
        }
    }
}
