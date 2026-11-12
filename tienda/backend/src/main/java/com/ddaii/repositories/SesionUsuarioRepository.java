package com.ddaii.repositories;

import com.ddaii.domain.SesionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SesionUsuarioRepository extends JpaRepository<SesionUsuario, Long> {

    SesionUsuario findByToken(String token);

    void deleteByUsuarioId(Long usuarioId);
}
