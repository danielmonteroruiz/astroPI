package com.astropi.astropi.controller.dto.common;

public class TicketResumenResponse {

    private long total;
    private long abiertas;
    private long enProceso;
    private long cerradas;

    public TicketResumenResponse() {
    }

    public TicketResumenResponse(long total, long abiertas, long enProceso, long cerradas) {
        this.total = total;
        this.abiertas = abiertas;
        this.enProceso = enProceso;
        this.cerradas = cerradas;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getAbiertas() {
        return abiertas;
    }

    public void setAbiertas(long abiertas) {
        this.abiertas = abiertas;
    }

    public long getEnProceso() {
        return enProceso;
    }

    public void setEnProceso(long enProceso) {
        this.enProceso = enProceso;
    }

    public long getCerradas() {
        return cerradas;
    }

    public void setCerradas(long cerradas) {
        this.cerradas = cerradas;
    }
}
