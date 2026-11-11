package com.tporopa.backend.soap;

import com.tporopa.backend.service.TallesService;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

/**
 * Endpoint SOAP de equivalencias de talles (P2, Día 4).
 * WSDL publicado en /ws/talles.wsdl.
 * Prendas: remeras, pantalones y calzado. Sistemas: AR, US, EU.
 */
@Endpoint
public class TallesEndpoint {

    private static final String NAMESPACE_URI = "http://tporopa.com/webropa/soap/talles";

    private final TallesService tallesService;

    public TallesEndpoint(TallesService tallesService) {
        this.tallesService = tallesService;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "EquivalenciaTallesRequest")
    @ResponsePayload
    public EquivalenciaTallesResponse equivalenciaTalles(@RequestPayload EquivalenciaTallesRequest request) {
        String resultado = tallesService.equivalencia(
                request.getPrenda(),
                request.getTalleOrigen(),
                request.getSistemaOrigen(),
                request.getSistemaDestino()
        );

        return new EquivalenciaTallesResponse(
                request.getPrenda(),
                request.getTalleOrigen(),
                request.getSistemaOrigen(),
                request.getSistemaDestino(),
                obtenerTalleDestino(resultado),
                resultado
        );
    }

    private String obtenerTalleDestino(String resultado) {
        if (resultado == null || !resultado.contains("=")) {
            return "";
        }
        String[] partes = resultado.split("=");
        if (partes.length < 2) return "";
        String destino = partes[1].trim();
        // destino tiene forma "TALLE (SISTEMA)"
        int idx = destino.indexOf(' ');
        return idx > 0 ? destino.substring(0, idx).trim() : destino;
    }
}
