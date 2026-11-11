package com.ddaii.soap;

import com.ddaii.services.TallesService;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.List;
import java.util.Map;

/**
 * Endpoint SOAP de equivalencias de talles (docs/PLAN-INTEGRACION.md 3).
 * WSDL publicado en /ws/equivalencias-talles.wsdl.
 * Prendas: remeras, pantalones y calzado. Países/sistemas: AR, US, EU.
 */
@Endpoint
public class TallesEndpoint {

    private static final String NAMESPACE_URI = "http://ddaii.com/equivalencias";

    private final TallesService tallesService;

    public TallesEndpoint(TallesService tallesService) {
        this.tallesService = tallesService;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "EquivalenciaRequest")
    @ResponsePayload
    public EquivalenciaResponse equivalencia(@RequestPayload EquivalenciaRequest request) {
        Map<String, String> equivalencias = tallesService.equivalencias(
                request.getTipoPrenda(), request.getTalle(), request.getPais());

        List<EquivalenciaResponse.Equivalencia> lista = equivalencias.entrySet().stream()
                .map(e -> new EquivalenciaResponse.Equivalencia(e.getKey(), e.getValue()))
                .toList();

        return new EquivalenciaResponse(
                request.getPais(),
                request.getTipoPrenda(),
                request.getTalle(),
                lista
        );
    }
}
