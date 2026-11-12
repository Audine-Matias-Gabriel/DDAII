package com.ddaii.security;

import com.ddaii.domain.SesionUsuario;
import com.ddaii.domain.Usuario;
import com.ddaii.repositories.SesionUsuarioRepository;
import com.ddaii.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TokenService {

    private static final int TTL_HOURS = 168; // 7 días

    private final SesionUsuarioRepository sesionUsuarioRepository;
    private final UsuarioRepository usuarioRepository;

    public TokenService(SesionUsuarioRepository sesionUsuarioRepository,
                        UsuarioRepository usuarioRepository) {
        this.sesionUsuarioRepository = sesionUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Genera un token opaco para el usuario y lo persiste con fecha de expiración.
     */
    @Transactional
    public String generarToken(Usuario usuario) {
        // Invalidar sesiones previas del usuario (un solo dispositivo por simplicidad)
        sesionUsuarioRepository.deleteByUsuarioId(usuario.getId());

        String token = UUID.randomUUID().toString();
        LocalDateTime expiraEn = LocalDateTime.now().plusHours(TTL_HOURS);

        SesionUsuario sesion = new SesionUsuario(usuario, token, expiraEn);
        sesionUsuarioRepository.save(sesion);

        return token;
    }

    /**
     * Valida un token. Retorna el usuario si es válido y no expirado, null en caso contrario.
     */
    public Usuario validarToken(String token) {
        SesionUsuario sesion = sesionUsuarioRepository.findByToken(token);
        if (sesion == null) {
            return null;
        }
        if (sesion.getExpiraEn().isBefore(LocalDateTime.now())) {
            sesionUsuarioRepository.delete(sesion);
            return null;
        }
        return sesion.getUsuario();
    }

    /**
     * Invalida un token (logout).
     */
    @Transactional
    public void invalidarToken(String token) {
        SesionUsuario sesion = sesionUsuarioRepository.findByToken(token);
        if (sesion != null) {
            sesionUsuarioRepository.delete(sesion);
        }
    }
}
