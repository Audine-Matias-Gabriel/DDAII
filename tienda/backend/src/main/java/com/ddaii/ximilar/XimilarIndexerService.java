package com.ddaii.ximilar;

import com.ddaii.domain.Producto;
import com.ddaii.repositories.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class XimilarIndexerService {

    private static final Logger log = LoggerFactory.getLogger(XimilarIndexerService.class);

    private final ProductoRepository productoRepository;
    private final XimilarFacade ximilarFacade;

    public XimilarIndexerService(ProductoRepository productoRepository, XimilarFacade ximilarFacade) {
        this.productoRepository = productoRepository;
        this.ximilarFacade = ximilarFacade;
    }

    public int indexarCatalogo() {
        int indexados = 0;
        for (Producto producto : productoRepository.findAll()) {
            try {
                ximilarFacade.indexarProducto(producto);
                indexados++;
            } catch (Exception e) {
                log.warn("No se pudo indexar producto {}: {}", producto.getId(), e.getMessage());
            }
        }
        log.info("Indexación Ximilar finalizada: {} productos", indexados);
        return indexados;
    }
}