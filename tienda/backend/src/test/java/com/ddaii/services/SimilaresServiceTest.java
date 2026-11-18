package com.ddaii.services;

import com.ddaii.domain.Producto;
import com.ddaii.domain.Tienda;
import com.ddaii.dto.ProductoSimilarDTO;
import com.ddaii.integrations.XimilarFacade;
import com.ddaii.repositories.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimilaresServiceTest {

    private final ProductoRepository repo = mock(ProductoRepository.class);
    private final XimilarFacade facade = mock(XimilarFacade.class);
    private final SimilaresService service = new SimilaresService(repo, facade);

    private static Tienda tienda(long id) {
        Tienda t = new Tienda("T" + id, "https://t" + id, true);
        t.setId(id);
        return t;
    }

    private static Producto producto(long id, Tienda tienda, String categoria) {
        Producto p = new Producto("e" + id, tienda, "P" + id, "", 100.0, categoria, "Unisex", "M",
                "https://t/" + id, "https://img/" + id, true);
        p.setId(id);
        return p;
    }

    @Test
    void conXimilarDevuelveSuOrdenYSoloOtrasTiendas() {
        Producto base = producto(1, tienda(1), "CALZADO");
        Producto a = producto(10, tienda(2), "CALZADO");
        Producto b = producto(11, tienda(2), "REMERA");
        Producto mismaTienda = producto(2, tienda(1), "CALZADO");
        when(repo.findById(1L)).thenReturn(Optional.of(base));
        when(facade.buscarSimilares(eq(base), anyInt())).thenReturn(List.of(11L, 2L, 10L));
        when(repo.findAllById(any())).thenReturn(List.of(a, mismaTienda, b)); // orden distinto

        List<ProductoSimilarDTO> res = service.buscarSimilares(1L, 4);

        assertThat(res).extracting(ProductoSimilarDTO::getId).containsExactly(11L, 10L);
        assertThat(res).allMatch(d -> d.getOrigen().equals("ximilar"));
    }

    @Test
    void sinXimilarUsaMismaCategoriaDeOtraTiendaPrimero() {
        Producto base = producto(1, tienda(1), "CALZADO");
        when(repo.findById(1L)).thenReturn(Optional.of(base));
        when(facade.buscarSimilares(eq(base), anyInt())).thenReturn(List.of());
        when(repo.findByTienda_IdNotAndIdNot(anyLong(), anyLong(), any(Pageable.class))).thenReturn(List.of(
                producto(20, tienda(2), "REMERA"),
                producto(21, tienda(2), "CALZADO"),
                producto(22, tienda(2), "PANTALON")));

        List<ProductoSimilarDTO> res = service.buscarSimilares(1L, 2);

        assertThat(res).extracting(ProductoSimilarDTO::getId).containsExactly(21L, 20L);
        assertThat(res).allMatch(d -> d.getOrigen().equals("catalogo"));
    }
}
