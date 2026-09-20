package br.com.fidelidade.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import java.awt.*;
import java.awt.image.BufferedImage;

@Configuration
@EnableConfigurationProperties(NotificacaoConfig.class)
public class DesktopConfig {
    private final JavaMailSender mailSender;
    private final NotificacaoConfig config;
    public DesktopConfig(JavaMailSender mailSender, NotificacaoConfig config) { this.mailSender = mailSender; this.config = config; }
    @PostConstruct
    public void notificarAbertura() {
        String mensagem = "Sistema de fidelidade aberto em " + java.time.LocalDateTime.now();
        if (!GraphicsEnvironment.isHeadless()) {
            EventQueue.invokeLater(() -> TrayNotification.show("Fidelidade", mensagem));
        }
        if (config.getDestinatario() != null && !config.getDestinatario().isBlank()) {
            try { var email = new SimpleMailMessage(); email.setTo(config.getDestinatario()); email.setSubject("Sistema de fidelidade aberto"); email.setText(mensagem); mailSender.send(email); }
            catch (Exception ignored) { /* indisponibilidade de e-mail não impede o uso local */ }
        }
    }
    static final class TrayNotification {
        static void show(String titulo, String texto) {
            try {
                if (!SystemTray.isSupported()) return;
                var tray = SystemTray.getSystemTray();
                var image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                var icon = new TrayIcon(image, "Fidelidade"); icon.setImageAutoSize(true); tray.add(icon);
                icon.displayMessage(titulo, texto, TrayIcon.MessageType.INFO);
                new java.util.Timer().schedule(new java.util.TimerTask() { public void run() { tray.remove(icon); } }, 10000);
            } catch (AWTException ignored) { }
        }
    }
}
