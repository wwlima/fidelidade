package br.com.fidelidade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fidelidade.notificacao")
public class NotificacaoConfig {
    private String destinatario = "";
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
}
