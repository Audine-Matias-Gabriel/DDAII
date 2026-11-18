package com.ddaii.services;

import com.ddaii.domain.Usuario;
import com.ddaii.repositories.UsuarioRepository;
import com.ddaii.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UsuarioRepository repo = mock(UsuarioRepository.class);
    private final TokenService tokens = mock(TokenService.class);
    private final AuthService service = new AuthService(repo, tokens, new BCryptPasswordEncoder());

    @Test
    void registraConDatosValidosYGuardaLaContrasenaHasheada() {
        when(repo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tokens.generarToken(any())).thenReturn("token");

        var respuesta = service.registrar("Ana", " ana@example.test ", "secreta123");

        assertThat(respuesta.getToken()).isEqualTo("token");
        assertThat(respuesta.getUsuario().getEmail()).isEqualTo("ana@example.test");
        verify(repo).save(any(Usuario.class));
    }

    @Test
    void rechazaDatosInvalidosSinGuardarNada() {
        assertThatThrownBy(() -> service.registrar("", "ana@example.test", "secreta123"))
                .hasMessageContaining("nombre");
        assertThatThrownBy(() -> service.registrar("Ana", "no-es-email", "secreta123"))
                .hasMessageContaining("email");
        assertThatThrownBy(() -> service.registrar("Ana", "ana@example.test", "123"))
                .hasMessageContaining("6 caracteres");
        assertThatThrownBy(() -> service.registrar("Ana", "ana@example.test", null))
                .hasMessageContaining("6 caracteres");
        verify(repo, never()).save(any());
    }

    @Test
    void rechazaUnEmailYaRegistrado() {
        when(repo.findByEmail("ana@example.test")).thenReturn(new Usuario("Ana", "ana@example.test", "hash"));

        assertThatThrownBy(() -> service.registrar("Ana", "ana@example.test", "secreta123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está registrado");
    }
}
