package br.com.fidelidade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class FidelidadeApplication {
    private static final CountDownLatch ENCERRAMENTO_DESKTOP = new CountDownLatch(1);

    public static void main(String[] args) {
        // Definir antes de qualquer acesso às classes AWT/Swing. Isso evita que uma
        // propriedade herdada do ambiente ou da IDE force a execução headless.
        System.setProperty("java.awt.headless", "false");

        SpringApplication aplicacao = new SpringApplication(FidelidadeApplication.class);
        aplicacao.setHeadless(false);
        ConfigurableApplicationContext contexto = aplicacao.run(args);

        // O Spring Boot não web encerra o processo quando o método main retorna.
        // Aguardar sempre é necessário para manter a aplicação desktop aberta.
        try {
            ENCERRAMENTO_DESKTOP.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } finally {
            contexto.close();
        }
    }

    public static void encerrar() {
        ENCERRAMENTO_DESKTOP.countDown();
    }
}
