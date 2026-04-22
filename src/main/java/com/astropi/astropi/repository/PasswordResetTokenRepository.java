package com.astropi.astropi.repository;

import com.astropi.astropi.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHashAndUsadoEnIsNullAndExpiraEnAfter(String tokenHash, LocalDateTime now);

    List<PasswordResetToken> findByUsuarioIdAndUsadoEnIsNullAndExpiraEnAfter(Long usuarioId, LocalDateTime now);
}
