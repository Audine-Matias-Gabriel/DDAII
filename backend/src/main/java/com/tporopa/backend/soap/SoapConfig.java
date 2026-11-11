package com.tporopa.backend.soap;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;

/**
 * Configuración de Spring Web Services (SOAP).
 * Publica el WSDL de talles en /ws/talles.wsdl.
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

    @Bean(name = "talles")
    public DefaultWsdl11Definition defaultWsdl11Definition(ApplicationContext context) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("TallesPort");
        wsdl11Definition.setLocationUri("/ws");
        wsdl11Definition.setSchema(new ClassPathResource("xsd/talles.xsd"));
        return wsdl11Definition;
    }
}
