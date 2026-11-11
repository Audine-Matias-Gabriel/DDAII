package com.ddaii.services;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Servicio de equivalencias de talles para el endpoint SOAP (P2, Día 4).
 * Talles para remeras, pantalones y calzado, sistemas AR / US / EU.
 */
@Service
public class TallesService {

    private static final Map<String, Map<String, String>> TALLES = new HashMap<>();

    static {
        // remeras
        Map<String, String> remeras = new HashMap<>();
        remeras.put("remera|S|AR|US", "S"); remeras.put("remera|S|AR|EU", "S");
        remeras.put("remera|M|AR|US", "M"); remeras.put("remera|M|AR|EU", "M");
        remeras.put("remera|L|AR|US", "L"); remeras.put("remera|L|AR|EU", "L");
        remeras.put("remera|S|US|AR", "S"); remeras.put("remera|S|US|EU", "S");
        remeras.put("remera|M|US|AR", "M"); remeras.put("remera|M|US|EU", "M");
        remeras.put("remera|L|US|AR", "L"); remeras.put("remera|L|US|EU", "L");
        remeras.put("remera|S|EU|AR", "S"); remeras.put("remera|S|EU|US", "S");
        remeras.put("remera|M|EU|AR", "M"); remeras.put("remera|M|EU|US", "M");
        remeras.put("remera|L|EU|AR", "L"); remeras.put("remera|L|EU|US", "L");
        TALLES.put("remera", remeras);

        // pantalones
        Map<String, String> pantalones = new HashMap<>();
        pantalones.put("pantalon|40|AR|US", "30"); pantalones.put("pantalon|40|AR|EU", "40");
        pantalones.put("pantalon|42|AR|US", "32"); pantalones.put("pantalon|42|AR|EU", "42");
        pantalones.put("pantalon|30|US|AR", "40"); pantalones.put("pantalon|30|US|EU", "40");
        pantalones.put("pantalon|40|EU|AR", "40"); pantalones.put("pantalon|40|EU|US", "30");
        TALLES.put("pantalon", pantalones);

        // calzado
        Map<String, String> calzado = new HashMap<>();
        calzado.put("calzado|38|AR|US", "6"); calzado.put("calzado|38|AR|EU", "38");
        calzado.put("calzado|39|AR|US", "7"); calzado.put("calzado|39|AR|EU", "39");
        calzado.put("calzado|6|US|AR", "38"); calzado.put("calzado|6|US|EU", "39");
        calzado.put("calzado|38|EU|AR", "38"); calzado.put("calzado|38|EU|US", "6");
        TALLES.put("calzado", calzado);
    }

    public String equivalencia(String prenda, String talleOrigen, String sistemaOrigen, String sistemaDestino) {
        String clave = prenda.toLowerCase().trim() + "|" + talleOrigen.trim() + "|"
                + sistemaOrigen.trim().toUpperCase() + "|" + sistemaDestino.trim().toUpperCase();
        Map<String, String> prendaMap = TALLES.get(prenda.toLowerCase().trim());
        if (prendaMap == null) {
            return "No hay equivalencia registrada para la prenda: " + prenda;
        }
        String resultado = prendaMap.get(clave);
        if (resultado == null) {
            return "No se encontró equivalencia de " + sistemaOrigen + " " + talleOrigen
                    + " a " + sistemaDestino + " para " + prenda;
        }
        return talleOrigen + " (" + sistemaOrigen + ") = " + resultado + " (" + sistemaDestino + ")";
    }

    /**
     * Equivalencias de un talle hacia los demás sistemas (docs/PLAN-INTEGRACION.md 5).
     */
    public Map<String, String> equivalencias(String prenda, String talle, String sistemaOrigen) {
        String prendaNormalizada = normalizarPrenda(prenda);
        String origen = sistemaOrigen != null ? sistemaOrigen.trim().toUpperCase() : "";

        Map<String, String> resultado = new LinkedHashMap<>();
        for (String destino : List.of("AR", "US", "EU")) {
            if (destino.equals(origen)) {
                continue;
            }
            String equivalencia = equivalencia(prendaNormalizada, talle, origen, destino);
            if (equivalencia != null && !equivalencia.startsWith("No ")) {
                resultado.put(destino, extraerTalleDestino(equivalencia));
            }
        }
        return resultado;
    }

    private String normalizarPrenda(String prenda) {
        if (prenda == null) {
            return "";
        }
        return switch (prenda.trim().toUpperCase()) {
            case "REMERA" -> "remera";
            case "PANTALON", "PANTALÓN" -> "pantalon";
            case "CALZADO", "ZAPATILLA", "ZAPATO" -> "calzado";
            default -> prenda.trim().toLowerCase();
        };
    }

    private String extraerTalleDestino(String equivalencia) {
        int igual = equivalencia.indexOf('=');
        if (igual < 0) {
            return "";
        }
        String destino = equivalencia.substring(igual + 1).trim();
        int espacio = destino.indexOf(' ');
        return espacio > 0 ? destino.substring(0, espacio).trim() : destino;
    }
}
