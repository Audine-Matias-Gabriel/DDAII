package com.ddaii.soap;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

/**
 * Configuración de Spring Web Services (SOAP).
 * Publica el WSDL de equivalencias en /ws/equivalencias.wsdl.
 */
@EnableWs
@Configuration
public class SoapConfig extends WsConfigurerAdapter {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean
    public XsdSchema equivalenciasSchema() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/equivalencias-talles.xsd"));
    }

    @Bean(name = "equivalencias-talles")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema equivalenciasSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("EquivalenciasTallesPort");
        wsdl11Definition.setLocationUri("/ws");
        wsdl11Definition.setSchema(equivalenciasSchema);
        return wsdl11Definition;
    }
}
