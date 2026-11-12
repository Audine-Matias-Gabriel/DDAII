package com.ddaii.services;

import com.ddaii.domain.Producto;
import com.ddaii.domain.Usuario;
import com.ddaii.dto.FavoritoDTO;
import com.ddaii.exceptions.RecursoNoEncontradoException;
import com.ddaii.repositories.FavoritoRepository;
import com.ddaii.repositories.ProductoRepository;
import com.ddaii.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public FavoritoService(FavoritoRepository favoritoRepository,
                           UsuarioRepository usuarioRepository,
                           ProductoRepository productoRepository) {
        this.favoritoRepository = favoritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Lista los productos favoritos de un usuario.
     */
    public List<FavoritoDTO> listarFavoritos(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + usuarioId));

        return favoritoRepository.findByUsuarioId(usuarioId).stream()
                .map(f -> {
                    Producto p = f.getProducto();
                    return new FavoritoDTO(
                            p.getId(), p.getNombre(), p.getPrecio(),
                            p.getImagenUrl(), p.getDisponible());
                })
                .collect(Collectors.toList());
    }

    /**
     * Agrega un producto a los favoritos del usuario. Idempotente: si ya existe, no hace nada.
     */
    @Transactional
    public void agregarFavorito(Long usuarioId, Long productoId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + usuarioId));
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + productoId));

        if (!favoritoRepository.existsByUsuarioIdAndProductoId(usuarioId, productoId)) {
            favoritoRepository.save(new Favorito(usuario, producto));
        }
    }

    /**
     * Quita un producto de los favoritos del usuario.
     */
    @Transactional
    public void quitarFavorito(Long usuarioId, Long productoId) {
        if (!favoritoRepository.existsByUsuarioIdAndProductoId(usuarioId, productoId)) {
            throw new RecursoNoEncontradoException(
                    "El producto no está en favoritos del usuario: " + productoId);
        }
        favoritoRepository.deleteByUsuarioIdAndProductoId(usuarioId, productoId);
    }

    /**
     * Indica si un producto está en favoritos del usuario.
     */
    public boolean esFavorito(Long usuarioId, Long productoId) {
        return favoritoRepository.existsByUsuarioIdAndProductoId(usuarioId, productoId);
    }
}
