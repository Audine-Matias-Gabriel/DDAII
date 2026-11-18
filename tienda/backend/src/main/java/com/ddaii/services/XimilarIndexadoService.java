package com.ddaii.services;

import com.ddaii.domain.Producto;
import com.ddaii.integrations.XimilarClient;
import com.ddaii.integrations.XimilarFacade;
import com.ddaii.repositories.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Sube a la colección de Ximilar las imágenes del catálogo que todavía no están
 * (o cuya imagen cambió), de a 10 por llamada.
 *
 * Cuida los créditos del plan gratuito: cada corrida sube como máximo {@code maximo}
 * imágenes (0,25 créditos cada una) y nunca pasa de {@code ximilar.maximo-coleccion}
 * registros (la colección gratuita admite 1.000).
 *
 * Corre sola cada 10 minutos si XIMILAR_INDEXADO_AUTOMATICO=true (así se suben los
 * productos nuevos del scraper) o a mano con POST /api/similares/indexar.
 */
@Service
public class XimilarIndexadoService {

    private static final Logger log = LoggerFactory.getLogger(XimilarIndexadoService.class);
    public static final double CREDITOS_POR_IMAGEN = 0.25;

    private final ProductoRepository productoRepository;
    private final XimilarFacade ximilarFacade;
    private final boolean automatico;
    private final int maximoPorCorrida;
    private final int maximoColeccion;

    public XimilarIndexadoService(ProductoRepository productoRepository,
                                  XimilarFacade ximilarFacade,
                                  @Value("${ximilar.indexado-automatico:false}") boolean automatico,
                                  @Value("${ximilar.maximo-por-corrida:100}") int maximoPorCorrida,
                                  @Value("${ximilar.maximo-coleccion:1000}") int maximoColeccion) {
        this.productoRepository = productoRepository;
        this.ximilarFacade = ximilarFacade;
        this.automatico = automatico;
        this.maximoPorCorrida = maximoPorCorrida;
        this.maximoColeccion = maximoColeccion;
    }

    /** Resumen de una corrida, con los créditos estimados que gastó. */
    public record Resumen(boolean activo, String mensaje, long pendientesAntes, int subidos, int fallidos,
                          long enColeccion, long pendientesDespues, double creditosEstimados) {}

    @Scheduled(initialDelayString = "${ximilar.indexado-demora-inicial-ms:60000}",
               fixedDelayString = "${ximilar.indexado-cada-ms:600000}")
    public void indexarAutomatico() {
        if (!automatico || !ximilarFacade.activo()) return;
        if (productoRepository.contarPendientesDeXimilar() == 0) return;
        Resumen r = indexarPendientes(maximoPorCorrida);
        log.info("Ximilar (automático): {}", r.mensaje());
    }

    /** Sube hasta {@code maximo} productos pendientes. */
    public Resumen indexarPendientes(int maximo) {
        long pendientesAntes = productoRepository.contarPendientesDeXimilar();
        long enColeccion = productoRepository.countByXimilarImagenUrlIsNotNull();

        if (!ximilarFacade.activo()) {
            String motivo = ximilarFacade.modoMock()
                    ? "XIMILAR_MOCK=true: no se sube nada."
                    : "Faltan XIMILAR_API_KEY y XIMILAR_COLLECTION_ID: no se sube nada.";
            return new Resumen(false, motivo, pendientesAntes, 0, 0, enColeccion, pendientesAntes, 0);
        }

        long lugar = Math.max(0, maximoColeccion - enColeccion);
        int tope = (int) Math.min(Math.min(Math.max(maximo, 0), maximoPorCorrida), lugar);
        if (tope == 0) {
            String msg = lugar == 0
                    ? "La colección llegó al máximo (" + maximoColeccion + "); no se sube nada."
                    : "Nada para subir.";
            return new Resumen(true, msg, pendientesAntes, 0, 0, enColeccion, pendientesAntes, 0);
        }

        List<Producto> pendientes = productoRepository.pendientesDeXimilar(PageRequest.of(0, tope));
        int subidos = 0;
        int fallidos = 0;
        for (int i = 0; i < pendientes.size(); i += XimilarClient.MAX_REGISTROS_POR_INSERT) {
            List<Producto> lote = pendientes.subList(i, Math.min(i + XimilarClient.MAX_REGISTROS_POR_INSERT, pendientes.size()));
            Set<Long> ok = ximilarFacade.subir(lote);
            for (Producto p : lote) {
                if (ok.contains(p.getId())) {
                    p.setXimilarImagenUrl(p.getImagenUrl());
                    subidos++;
                } else {
                    fallidos++;
                }
            }
            productoRepository.saveAll(lote);
        }

        long pendientesDespues = productoRepository.contarPendientesDeXimilar();
        long enColeccionDespues = productoRepository.countByXimilarImagenUrlIsNotNull();
        double creditos = subidos * CREDITOS_POR_IMAGEN;
        String msg = String.format("subidas %d imágenes (~%.2f créditos), fallidas %d, en la colección %d, pendientes %d",
                subidos, creditos, fallidos, enColeccionDespues, pendientesDespues);
        log.info("Ximilar: {}", msg);
        return new Resumen(true, msg, pendientesAntes, subidos, fallidos, enColeccionDespues, pendientesDespues, creditos);
    }
}
