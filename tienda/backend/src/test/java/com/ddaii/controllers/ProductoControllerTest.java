package com.ddaii.controllers;

import com.ddaii.services.ProductoService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductoControllerTest {

    /** "Mostrar más productos" pide página por página: el orden tiene que ser siempre el mismo. */
    @Test
    void listarPaginaConOrdenFijoPorId() {
        ProductoService service = mock(ProductoService.class);
        when(service.filtrar(any(), any(), any(), any(), any(), any(), any())).thenReturn(Page.empty());

        new ProductoController(service).listar(null, 2L, null, null, null, null, 3, 20);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(service).filtrar(isNull(), any(), isNull(), isNull(), isNull(), isNull(), pageable.capture());
        assertEquals(3, pageable.getValue().getPageNumber());
        assertEquals(20, pageable.getValue().getPageSize());
        assertEquals(Sort.by("id"), pageable.getValue().getSort());
    }
}
