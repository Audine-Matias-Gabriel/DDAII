package com.ddaii.config;

import com.ddaii.domain.Producto;
import com.ddaii.repositories.ProductoRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.List;

@Configuration
public class ProductoDataLoader {

    @Bean
    CommandLineRunner cargarProductos(
            ProductoRepository productoRepository,
            JsonMapper jsonMapper) {

        return args -> {

            if (productoRepository.count() > 0) {
                System.out.println(
                        "[INFO] La base ya contiene productos. " +
                                "No se carga nuevamente el JSON."
                );
                return;
            }

            ClassPathResource resource =
                    new ClassPathResource("productos.json");

            try (InputStream inputStream =
                         resource.getInputStream()) {

                List<Producto> productos =
                        jsonMapper.readValue(
                                inputStream,
                                new TypeReference<List<Producto>>() {}
                        );

                productoRepository.saveAll(productos);

                System.out.println(
                        "[INFO] Productos cargados desde productos.json: "
                                + productos.size()
                );
            }
        };
    }
}