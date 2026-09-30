package com.ddaii.repositories;

import com.ddaii.domain.Categoria;
import com.ddaii.domain.Producto;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ProductoRepositoryMemoria implements ProductoRepository {

    private final Map<Long, Producto> productos = new LinkedHashMap<>();

    private long siguienteId = 1;

    /**
     * Se ejecuta automáticamente cuando Spring crea el Repository.
     */
    @PostConstruct
    public void cargarProductos() {

        try (BufferedReader reader = abrirArchivoProductos()) {

            String linea;

            while ((linea = reader.readLine()) != null) {

                linea = linea.trim();

                // Ignorar líneas vacías
                if (linea.isEmpty()) {
                    continue;
                }

                // Ignorar comentarios
                if (linea.startsWith("#")) {
                    continue;
                }

                Producto producto = convertirLineaAProducto(linea);

                productos.put(producto.getId(), producto);

                if (producto.getId() >= siguienteId) {
                    siguienteId = producto.getId() + 1;
                }
            }

            System.out.println(
                    "[INFO] Productos cargados: " + productos.size());

        } catch (IOException e) {

            throw new IllegalStateException(
                    "No se pudo leer el archivo productos.txt",
                    e);
        }
    }

    /**
     * Busca productos.txt en las ubicaciones habituales:
     *
     * 1. Directorio actual
     * 2. Directorio padre
     * 3. src/main/resources
     */
    private BufferedReader abrirArchivoProductos() throws IOException {

        Path rutaActual = Path.of("productos.txt");

        if (Files.exists(rutaActual)) {
            return Files.newBufferedReader(
                    rutaActual,
                    StandardCharsets.UTF_8
            );
        }

        Path rutaPadre = Path.of("../productos.txt");

        if (Files.exists(rutaPadre)) {
            return Files.newBufferedReader(
                    rutaPadre,
                    StandardCharsets.UTF_8
            );
        }

        InputStream recurso =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream("productos.txt");

        if (recurso != null) {

            return new BufferedReader(
                    new InputStreamReader(
                            recurso,
                            StandardCharsets.UTF_8
                    )
            );
        }

        throw new IOException(
                "No se encontró productos.txt"
        );
    }

    /**
     * Convierte una línea del archivo en un objeto Producto.
     *
     * Formato:
     *
     * id;nombre;descripcion;precio;stock;categoria;tiendaId;imagenUrl
     */
    private Producto convertirLineaAProducto(String linea) {

        String[] campos = linea.split(";", -1);

        if (campos.length != 8 && campos.length != 6) {

            throw new IllegalArgumentException(
                    "Formato inválido en productos.txt: " + linea
            );
        }

        Long id = Long.parseLong(campos[0].trim());

        String nombre = campos[1].trim();

        String descripcion = campos[2].trim();

        BigDecimal precio =
                new BigDecimal(campos[3].trim());

        Integer stock =
                Integer.parseInt(campos[4].trim());

        Categoria categoria =
                Categoria.valueOf(
                        campos[5].trim().toUpperCase()
                );

        Long tiendaId = null;
        String imagenUrl = null;

        // Formato completo del diagrama
        if (campos.length == 8) {

            if (!campos[6].trim().isEmpty()) {
                tiendaId =
                        Long.parseLong(campos[6].trim());
            }

            imagenUrl = campos[7].trim();

            if (imagenUrl.isEmpty()) {
                imagenUrl = null;
            }
        }

        return new Producto(
                id,
                nombre,
                descripcion,
                precio,
                stock,
                categoria,
                tiendaId,
                imagenUrl
        );
    }

    @Override
    public Optional<Producto> findById(Long id) {

        return Optional.ofNullable(
                productos.get(id)
        );
    }

    @Override
    public List<Producto> findByTiendaId(Long tiendaId) {

        return productos.values()
                .stream()
                .filter(producto ->
                        producto.getTiendaId() != null
                                && producto.getTiendaId().equals(tiendaId)
                )
                .toList();
    }

    @Override
    public List<Producto> findAll() {

        return new ArrayList<>(productos.values());
    }

    @Override
    public Producto save(Producto producto) {

        if (producto.getId() == null) {

            producto.setId(siguienteId++);
        }

        productos.put(
                producto.getId(),
                producto
        );

        return producto;
    }

    @Override
    public void delete(Long id) {

        productos.remove(id);
    }
}