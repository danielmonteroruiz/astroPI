package com.astropi.astropi.controller.dto;

import com.astropi.astropi.model.EstadoIncidencia;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EstadoRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deberiaAceptarEstadoCerrada() throws Exception {
        EstadoRequest request = objectMapper.readValue(
                "{\"estado\":\"CERRADA\"}",
                EstadoRequest.class
        );

        assertThat(request.getEstado()).isEqualTo(EstadoIncidencia.CERRADA);
    }
}
