package com.astropi.astropi.controller.dto.peticion;

import com.astropi.astropi.model.EstadoPeticion;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EstadoPeticionRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deberiaAceptarEstadoCerrada() throws Exception {
        EstadoPeticionRequest request = objectMapper.readValue(
                "{\"estado\":\"CERRADA\"}",
                EstadoPeticionRequest.class
        );

        assertThat(request.getEstado()).isEqualTo(EstadoPeticion.CERRADA);
    }
}
