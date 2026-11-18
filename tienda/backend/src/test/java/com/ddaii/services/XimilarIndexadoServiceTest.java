package com.ddaii.services;

import com.ddaii.domain.Producto;
import com.ddaii.domain.Tienda;
import com.ddaii.integrations.XimilarFacade;
import com.ddaii.repositories.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class XimilarIndexadoServiceTest {

    private final ProductoRepository repo = mock(ProductoRepository.class);
    private final XimilarFacade facade = mock(XimilarFacade.class);

    private static List<Producto> productos(int cantidad) {
        Tienda tienda = new Tienda("Grid", "https://www.grid.com.ar", true);
        tienda.setId(2L);
        List<Producto> lista = new ArrayList<>();
        for (long i = 1; i <= cantidad; i++) {
            Producto p = new Producto("ext-" + i, tienda, "Remera " + i, "", 100.0, "REMERA",
                    "Hombre", "Marca", "https://tienda/" + i, "https://img/" + i + ".jpg", true);
            p.setId(i);
            lista.add(p);
        }
        return lista;
    }

    @Test
    void subeDeA10YMarcaLosSubidosConSuImagen() {
        List<Producto> pendientes = productos(25);
        when(facade.activo()).thenReturn(true);
        when(repo.contarPendientesDeXimilar()).thenReturn(25L, 1L);
        when(repo.countByXimilarImagenUrlIsNotNull()).thenReturn(0L, 24L);
        when(repo.pendientesDeXimilar(any(Pageable.class))).thenReturn(pendientes);
        // El producto 13 falla (por ejemplo, la imagen no se pudo bajar).
        when(facade.subir(anyList())).thenAnswer(inv -> {
            List<Producto> lote = inv.getArgument(0);
            return Set.copyOf(lote.stream().map(Producto::getId).filter(id -> id != 13L).toList());
        });

        XimilarIndexadoService.Resumen r = new XimilarIndexadoService(repo, facade, false, 100, 1000)
                .indexarPendientes(100);

        verify(facade, times(3)).subir(anyList()); // 10 + 10 + 5
        assertThat(r.subidos()).isEqualTo(24);
        assertThat(r.fallidos()).isEqualTo(1);
        assertThat(r.creditosEstimados()).isEqualTo(6.0); // 24 × 0,25
        assertThat(pendientes.get(0).indexadoEnXimilar()).isTrue();
        assertThat(pendientes.get(12).indexadoEnXimilar()).isFalse();
    }

    @Test
    void respetaElMaximoDeLaColeccionGratuita() {
        when(facade.activo()).thenReturn(true);
        when(repo.contarPendientesDeXimilar()).thenReturn(50L);
        when(repo.countByXimilarImagenUrlIsNotNull()).thenReturn(995L);
        when(repo.pendientesDeXimilar(any(Pageable.class))).thenAnswer(inv -> {
            Pageable page = inv.getArgument(0);
            assertThat(page.getPageSize()).isEqualTo(5); // solo entran 5 más
            return productos(5);
        });
        when(facade.subir(anyList())).thenReturn(Set.of(1L, 2L, 3L, 4L, 5L));

        var r = new XimilarIndexadoService(repo, facade, false, 100, 1000).indexarPendientes(100);

        assertThat(r.subidos()).isEqualTo(5);
    }

    @Test
    void sinCredencialesNoSubeNada() {
        when(facade.activo()).thenReturn(false);
        when(repo.contarPendientesDeXimilar()).thenReturn(10L);

        var r = new XimilarIndexadoService(repo, facade, true, 100, 1000).indexarPendientes(100);

        assertThat(r.activo()).isFalse();
        assertThat(r.mensaje()).contains("XIMILAR_API_KEY");
        verify(facade, never()).subir(anyList());
    }
}
