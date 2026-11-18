/**
 * Mensajes SOAP de equivalencias de talles.
 *
 * El XSD (xsd/equivalencias-talles.xsd) declara elementFormDefault="qualified":
 * los hijos (pais, tipoPrenda, talle...) van en el namespace del servicio.
 * Sin esto JAXB los esperaba sin namespace y una request hecha según el WSDL
 * llegaba con los campos en null (SOAP Fault 500).
 */
@XmlSchema(namespace = "http://ddaii.com/equivalencias", elementFormDefault = XmlNsForm.QUALIFIED)
package com.ddaii.soap;

import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;
