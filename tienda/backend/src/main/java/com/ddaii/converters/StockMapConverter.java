package com.ddaii.converters;

import com.ddaii.domain.Genero;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.EnumMap;
import java.util.Map;

/**
 * Persiste el stock por talle y género como un string JSON en una columna text.
 * JPA no soporta mapas anidados con @ElementCollection, así que se mapea el
 * atributo como un tipo básico con este converter.
 */
@Converter(autoApply = true)
public class StockMapConverter
        implements AttributeConverter<Map<Genero, Map<String, Integer>>, String> {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private static final TypeReference<Map<Genero, Map<String, Integer>>> TIPO =
            new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(
            Map<Genero, Map<String, Integer>> attribute) {

        if (attribute == null) {
            return null;
        }

        return JSON_MAPPER.writeValueAsString(attribute);
    }

    @Override
    public Map<Genero, Map<String, Integer>> convertToEntityAttribute(
            String dbData) {

        if (dbData == null || dbData.isBlank()) {
            return new EnumMap<>(Genero.class);
        }

        return JSON_MAPPER.readValue(dbData, TIPO);
    }
}
