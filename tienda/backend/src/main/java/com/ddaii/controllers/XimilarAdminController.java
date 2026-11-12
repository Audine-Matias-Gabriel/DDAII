package com.ddaii.controllers;

import com.ddaii.ximilar.XimilarIndexerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/ximilar")
public class XimilarAdminController {

    private final XimilarIndexerService indexerService;

    public XimilarAdminController(XimilarIndexerService indexerService) {
        this.indexerService = indexerService;
    }

    @PostMapping("/indexar")
    public Map<String, Object> indexar() {
        int indexados = indexerService.indexarCatalogo();
        return Map.of("indexados", indexados, "mensaje", "Indexación finalizada");
    }
}