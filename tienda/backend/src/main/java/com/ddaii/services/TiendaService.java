package com.ddaii.services;

import com.ddaii.dto.TiendaDTO;
import com.ddaii.repositories.TiendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Listado de tiendas (docs/PLAN-INTEGRACION.md 2.3).
 */
@Service
@Transactional(readOnly = true)
public class TiendaService {

    private final TiendaRepository tiendaRepository;

    public TiendaService(TiendaRepository tiendaRepository) {
        this.tiendaRepository = tiendaRepository;
    }

    public List<TiendaDTO> listar() {
        return tiendaRepository.findAll().stream()
                .map(ProductoService::toTiendaDto)
                .toList();
    }
}
