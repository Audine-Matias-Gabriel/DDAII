package com.ddaii.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Un @Query que borra o modifica necesita @Modifying: sin eso Spring lo ejecuta como
 * consulta de lectura y falla en tiempo de ejecución (pasó con DELETE /api/favoritos/{id}).
 */
class RepositoriosConsultasTest {

    private static final List<Class<?>> REPOSITORIOS = List.of(
            FavoritoRepository.class,
            ProductoRepository.class,
            ProductoSnapshotRepository.class,
            RecomendacionRepository.class,
            SesionUsuarioRepository.class,
            SuscripcionRepository.class,
            TiendaRepository.class,
            UsuarioRepository.class);

    @Test
    void lasConsultasQueModificanTienenModifying() {
        List<String> sinModifying = new ArrayList<>();
        for (Class<?> repo : REPOSITORIOS) {
            for (Method m : repo.getDeclaredMethods()) {
                Query query = m.getAnnotation(Query.class);
                if (query == null) continue;
                String jpql = query.value().trim().toUpperCase(Locale.ROOT);
                boolean modifica = jpql.startsWith("DELETE") || jpql.startsWith("UPDATE") || jpql.startsWith("INSERT");
                if (modifica && m.getAnnotation(Modifying.class) == null) {
                    sinModifying.add(repo.getSimpleName() + "." + m.getName());
                }
            }
        }
        assertTrue(sinModifying.isEmpty(), "Falta @Modifying en: " + sinModifying);
    }

    @Test
    void quitarFavoritoUsaUnaConsultaConModifying() throws NoSuchMethodException {
        Method borrar = FavoritoRepository.class.getMethod(
                "deleteByUsuarioIdAndProductoId", Long.class, Long.class);
        assertTrue(borrar.isAnnotationPresent(Modifying.class),
                "deleteByUsuarioIdAndProductoId necesita @Modifying");
    }
}
