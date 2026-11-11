package com.ddaii.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del servicio de equivalencias de talles (SOAP, P2, Día 4).
 */
class TallesServiceTest {

    private final TallesService service = new TallesService();

    @Test
    void remeraS_AR_a_US_es_S() {
        String r = service.equivalencia("remera", "S", "AR", "US");
        assertTrue(r.contains("S (US)"));
    }

    @Test
    void pantalon40_AR_a_US_es_30() {
        String r = service.equivalencia("pantalon", "40", "AR", "US");
        assertTrue(r.contains("30 (US)"));
    }

    @Test
    void calzado38_AR_a_US_es_6() {
        String r = service.equivalencia("calzado", "38", "AR", "US");
        assertTrue(r.contains("6 (US)"));
    }

    @Test
    void prendaInexistenteDevuelveMensaje() {
        String r = service.equivalencia("vestido", "S", "AR", "US");
        assertTrue(r.contains("No hay equivalencia registrada"));
    }

    @Test
    void equivalenciasDevuelveDestinosDelContratoSoap() {
        var resultado = service.equivalencias("REMERA", "M", "AR");
        assertEquals("M", resultado.get("US"));
        assertEquals("M", resultado.get("EU"));
        assertFalse(resultado.containsKey("AR"));
    }
}
