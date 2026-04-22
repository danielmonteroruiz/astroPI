package com.astropi.astropi.service;

import com.astropi.astropi.controller.dto.auth.ForgotPasswordRequest;
import com.astropi.astropi.model.PasswordResetToken;
import com.astropi.astropi.model.Usuario;
import com.astropi.astropi.repository.PasswordResetTokenRepository;
import com.astropi.astropi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional
public class PasswordResetService {

    private static final String MENSAJE_GENERICO =
            "Si la cuenta existe y puede recuperarse por autoservicio, enviaremos instrucciones al correo registrado";

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PeticionService peticionService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();
    private final int expirationMinutes;
    private final String frontendResetUrl;

    public PasswordResetService(UsuarioRepository usuarioRepository,
                                PasswordResetTokenRepository passwordResetTokenRepository,
                                PeticionService peticionService,
                                MailService mailService,
                                PasswordEncoder passwordEncoder,
                                @Value("${app.password-reset.token-expiration-minutes}") int expirationMinutes,
                                @Value("${app.password-reset.frontend-url}") String frontendResetUrl) {
        this.usuarioRepository = usuarioRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.peticionService = peticionService;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.expirationMinutes = expirationMinutes;
        this.frontendResetUrl = frontendResetUrl;
    }

    public String solicitarRecuperacion(ForgotPasswordRequest request) {
        Optional<Usuario> usuarioOptional = buscarUsuario(request.getUsername(), request.getEmail());

        if (usuarioOptional.isEmpty()) {
            return MENSAJE_GENERICO;
        }

        Usuario usuario = usuarioOptional.get();

        if (esCuentaSensible(usuario)) {
            peticionService.crearSolicitudRecuperacionPasswordSensitiva(usuario, request);
            return MENSAJE_GENERICO;
        }

        if (!tieneEmailValido(usuario, request.getEmail())) {
            return MENSAJE_GENERICO;
        }

        emitirEnlaceReset(usuario, "AUTO_SERVICIO");
        return MENSAJE_GENERICO;
    }

    public String emitirEnlaceResetValidadoPorAdmin(ForgotPasswordRequest request) {
        Usuario usuario = buscarUsuario(request.getUsername(), request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no tiene email registrado");
        }

        emitirEnlaceReset(usuario, "ADMIN_VALIDADO");
        return "Enlace de recuperacion enviado al correo del usuario";
    }

    @Transactional(readOnly = true)
    public void validarToken(String tokenPlano) {
        obtenerTokenValido(tokenPlano);
    }

    public void resetearPassword(String tokenPlano, String nuevaPassword) {
        PasswordResetToken token = obtenerTokenValido(tokenPlano);
        Usuario usuario = token.getUsuario();

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setCredencialesActualizadasEn(LocalDateTime.now());
        usuarioRepository.save(usuario);

        invalidarTokensActivos(usuario);
        token.setUsadoEn(LocalDateTime.now());
        passwordResetTokenRepository.save(token);
    }

    private void emitirEnlaceReset(Usuario usuario, String motivo) {
        invalidarTokensActivos(usuario);

        String tokenPlano = generarTokenPlano();
        PasswordResetToken token = new PasswordResetToken();
        token.setUsuario(usuario);
        token.setTokenHash(calcularHash(tokenPlano));
        token.setMotivo(motivo);
        token.setCreadoEn(LocalDateTime.now());
        token.setExpiraEn(LocalDateTime.now().plusMinutes(expirationMinutes));

        passwordResetTokenRepository.save(token);

        String resetUrl = frontendResetUrl + "?token=" + tokenPlano;
        mailService.enviarCorreoResetPassword(usuario.getEmail(), usuario.getUsername(), resetUrl);
    }

    private void invalidarTokensActivos(Usuario usuario) {
        passwordResetTokenRepository.findByUsuarioIdAndUsadoEnIsNullAndExpiraEnAfter(usuario.getId(), LocalDateTime.now())
                .forEach(token -> token.setUsadoEn(LocalDateTime.now()));
    }

    private PasswordResetToken obtenerTokenValido(String tokenPlano) {
        String tokenHash = calcularHash(tokenPlano);

        return passwordResetTokenRepository
                .findByTokenHashAndUsadoEnIsNullAndExpiraEnAfter(tokenHash, LocalDateTime.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El enlace de recuperacion no es valido o ha expirado"));
    }

    private Optional<Usuario> buscarUsuario(String username, String email) {
        String usernameNormalizado = normalizar(username);
        String emailNormalizado = normalizar(email);

        if (usernameNormalizado != null) {
            Optional<Usuario> porUsername = usuarioRepository.findByUsername(usernameNormalizado);

            if (porUsername.isPresent()) {
                if (emailNormalizado == null) {
                    return porUsername;
                }

                return emailNormalizado.equalsIgnoreCase(normalizar(porUsername.get().getEmail()))
                        ? porUsername
                        : Optional.empty();
            }
        }

        if (emailNormalizado != null) {
            return usuarioRepository.findByEmail(emailNormalizado);
        }

        return Optional.empty();
    }

    private boolean tieneEmailValido(Usuario usuario, String emailSolicitado) {
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            return false;
        }

        String emailNormalizado = normalizar(emailSolicitado);
        return emailNormalizado == null || emailNormalizado.equalsIgnoreCase(normalizar(usuario.getEmail()));
    }

    private boolean esCuentaSensible(Usuario usuario) {
        return usuario.getRol() != null && "SUPER_ADMIN".equals(usuario.getRol().getNombre());
    }

    private String generarTokenPlano() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String calcularHash(String tokenPlano) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(tokenPlano.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();

            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }

            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }

    private String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
