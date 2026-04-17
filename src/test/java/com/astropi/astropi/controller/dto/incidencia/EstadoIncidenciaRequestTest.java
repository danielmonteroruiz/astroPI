package com.astropi.astropi.controller.dto.incidencia;

import com.astropi.astropi.model.EstadoIncidencia;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EstadoIncidenciaRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deberiaAceptarEstadoCerrada() throws Exception {
        EstadoIncidenciaRequest request = objectMapper.readValue(
                "{\"estado\":\"CERRADA\"}",
                EstadoIncidenciaRequest.class
        );

        assertThat(request.getEstado()).isEqualTo(EstadoIncidencia.CERRADA);
    }
}
