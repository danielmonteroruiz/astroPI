package com.astropi.astropi.controller.dto.common;

/**
 * Respuesta simple para operaciones que solo necesitan devolver un mensaje.
 */
public class MensajeResponse {

    private String mensaje;

    public MensajeResponse(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }
}
