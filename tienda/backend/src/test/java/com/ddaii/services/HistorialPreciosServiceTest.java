package com.ddaii.services;

import com.ddaii.domain.Producto;
import com.ddaii.domain.ProductoSnapshot;
import com.ddaii.dto.PrecioHistorialDTO;
import com.ddaii.exceptions.RecursoNoEncontradoException;
import com.ddaii.repositories.ProductoRepository;
import com.ddaii.repositories.ProductoSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistorialPreciosServiceTest {

    @Mock
    private ProductoSnapshotRepository snapshotRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private HistorialPreciosService service;

    private Producto productoConPrecio(Double precio) {
        Producto p = new Producto();
        p.setId(10L);
        p.setPrecio(precio);
        return p;
    }

    @Test
    void registrarSiCorresponde_guardaUnSnapshotPorDia() {
        Producto p = productoConPrecio(1500.0);
        when(snapshotRepository.findByProductoIdAndFecha(eq(10L), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        service.registrarSiCorresponde(p);

        ArgumentCaptor<ProductoSnapshot> captor = ArgumentCaptor.forClass(ProductoSnapshot.class);
        verify(snapshotRepository).save(captor.capture());
        assertEquals(LocalDate.now(), captor.getValue().getFecha());
        assertEquals(1500.0, captor.getValue().getPrecio());
        assertSame(p, captor.getValue().getProducto());
    }

    @Test
    void registrarSiCorresponde_actualizaElPrecioDelMismoDia() {
        Producto p = productoConPrecio(2000.0);
        ProductoSnapshot existente = new ProductoSnapshot(p, LocalDate.now(), 1800.0);
        when(snapshotRepository.findByProductoIdAndFecha(eq(10L), any(LocalDate.class)))
                .thenReturn(Optional.of(existente));

        service.registrarSiCorresponde(p);

        verify(snapshotRepository).save(existente);
        assertEquals(2000.0, existente.getPrecio());
    }

    @Test
    void registrarSiCorresponde_ignoraProductoSinIdOPrecio() {
        service.registrarSiCorresponde(new Producto());
        verifyNoInteractions(snapshotRepository);
    }

    @Test
    void historial_devuelveLaSerieOrdenadaPorFecha() {
        when(productoRepository.existsById(10L)).thenReturn(true);
        when(snapshotRepository.findByProductoIdOrderByFechaAsc(10L)).thenReturn(List.of(
                new ProductoSnapshot(null, LocalDate.of(2026, 11, 24), 1400.0),
                new ProductoSnapshot(null, LocalDate.of(2026, 11, 25), 1500.0)));

        List<PrecioHistorialDTO> historial = service.historial(10L);

        assertEquals(2, historial.size());
        assertEquals(LocalDate.of(2026, 11, 24), historial.get(0).getFecha());
        assertEquals(1400.0, historial.get(0).getPrecio());
        assertEquals(1500.0, historial.get(1).getPrecio());
    }

    @Test
    void historial_fallaSiElProductoNoExiste() {
        when(productoRepository.existsById(99L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> service.historial(99L));
        verifyNoInteractions(snapshotRepository);
    }
}
