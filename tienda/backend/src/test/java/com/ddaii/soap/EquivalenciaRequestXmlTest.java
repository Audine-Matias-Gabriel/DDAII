package com.ddaii.soap;

import jakarta.xml.bind.JAXBContext;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * La request tiene que leerse como dice el XSD (elementFormDefault="qualified"):
 * con los campos dentro del namespace del servicio.
 */
class EquivalenciaRequestXmlTest {

    @Test
    void leeLosCamposConNamespaceComoDiceElWsdl() throws Exception {
        String xml = """
                <eq:EquivalenciaRequest xmlns:eq="http://ddaii.com/equivalencias">
                  <eq:pais>AR</eq:pais>
                  <eq:tipoPrenda>CALZADO</eq:tipoPrenda>
                  <eq:talle>38</eq:talle>
                </eq:EquivalenciaRequest>
                """;

        EquivalenciaRequest request = (EquivalenciaRequest) JAXBContext.newInstance(EquivalenciaRequest.class)
                .createUnmarshaller()
                .unmarshal(new StringReader(xml));

        assertThat(request.getPais()).isEqualTo("AR");
        assertThat(request.getTipoPrenda()).isEqualTo("CALZADO");
        assertThat(request.getTalle()).isEqualTo("38");
    }
}
