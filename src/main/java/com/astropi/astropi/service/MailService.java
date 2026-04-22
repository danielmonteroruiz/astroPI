package com.astropi.astropi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String fromAddress;

    public MailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                       @Value("${app.mail.enabled:false}") boolean mailEnabled,
                       @Value("${app.mail.from}") String fromAddress) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.mailEnabled = mailEnabled;
        this.fromAddress = fromAddress;
    }

    public void enviarCorreoResetPassword(String destinatario, String username, String resetUrl) {
        if (!mailEnabled) {
            LOGGER.warn("Mail deshabilitado. Enlace de reset para {}: {}", username, resetUrl);
            return;
        }

        if (mailSender == null) {
            throw new IllegalStateException("Mail habilitado pero JavaMailSender no esta configurado");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(destinatario);
        message.setSubject("AstroPI - Recuperacion de password");
        message.setText(
                "Hola " + username + ",\n\n"
                        + "Hemos recibido una solicitud para restablecer tu password en AstroPI.\n"
                        + "Si has sido tu, usa este enlace para definir una nueva password:\n\n"
                        + resetUrl + "\n\n"
                        + "Este enlace caduca pronto y solo puede usarse una vez.\n"
                        + "Si no has solicitado este cambio, ignora este correo.\n"
        );

        mailSender.send(message);
    }
}
