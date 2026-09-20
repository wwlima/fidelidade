# Fidelidade Desktop

Aplicação desktop para o lojista cadastrar clientes em um programa de fidelidade. O aplicativo usa Java 25, Maven, Spring Boot, Swing, Spring Data JPA e banco H2 local.

## 1. Requisitos do sistema

### Funcionais
- RF01 — Permitir cadastro de cliente somente pela tela do lojista.
- RF02 — Exigir nome e e-mail; telefone é opcional.
- RF03 — Validar formato de e-mail e impedir e-mail duplicado.
- RF04 — Persistir clientes em banco local H2 baseado em arquivo.
- RF05 — Exibir confirmação ou erro na tela após o cadastro.
- RF06 — Ao abrir, exibir notificação nativa na tela quando suportada.
- RF07 — Ao abrir, enviar e-mail de notificação para o destinatário configurado.

### Não funcionais
- RNF01 — Java 25 e Maven.
- RNF02 — Execução sem servidor web embutido: aplicação local desktop.
- RNF03 — Dados armazenados localmente, sem exposição de API pública.
- RNF04 — Falha de SMTP não bloqueia a operação local.
- RNF05 — Restrição de ator: não existe fluxo de auto cadastro ou tela de cliente.

## 2. Arquitetura proposta

Arquitetura em camadas, executada como processo desktop único:

- **UI:** Java Swing (`TelaPrincipal`), única interface do lojista.
- **Serviço:** `ClienteService`, com regras de validação e prevenção de duplicidade.
- **Persistência:** Spring Data JPA + H2 em arquivo local (`./data/fidelidade`).
- **Infraestrutura:** Spring Boot inicializa o contexto; `DesktopConfig` dispara notificação nativa e e-mail na abertura.
- **Segurança de escopo:** somente a UI do lojista chama o serviço; não há endpoint REST nem cadastro público.

Fluxo principal: `Lojista → Tela Swing → ClienteService → ClienteRepository → H2`.

## 3. Notificação por e-mail e na tela ao abrir

A notificação é disparada no startup. Configure as variáveis de ambiente abaixo antes de executar:

```bash
export SMTP_HOST=smtp.exemplo.com
export SMTP_PORT=587
export SMTP_USERNAME=usuario
export SMTP_PASSWORD=senha
export SMTP_AUTH=true
export SMTP_STARTTLS=true
export NOTIFICACAO_EMAIL=lojista@exemplo.com
```

A notificação nativa usa o tray do sistema operacional quando disponível. Em ambiente headless, ela é ignorada; o e-mail continua sendo tentado se configurado.

## Executar

Pré-requisitos: JDK 25 e Maven 3.9+.

```bash
mvn spring-boot:run
```

Ou gerar o executável JAR:

```bash
mvn clean package
java -jar target/fidelidade-desktop-1.0.0.jar
```

A aplicação permanece aberta enquanto a janela do lojista estiver aberta. Para encerrá-la, feche a janela normalmente. Se for interrompida pelo terminal, use `Ctrl+C`.

### Windows: ambiente headless

Execute o JAR em um Prompt de Comando ou PowerShell aberto dentro da sessão do Windows que possui a área de trabalho. Não use `-Djava.awt.headless=true` e não execute o programa como serviço do Windows, tarefa agendada sem opção de interação ou em uma sessão remota sem área gráfica.

A aplicação também força `java.awt.headless=false` antes de iniciar o Spring Boot. Se ainda aparecer a mensagem de ambiente headless, o Java não está conseguindo acessar uma sessão gráfica do Windows; nesse caso, abra uma sessão local/área de trabalho remota ativa e execute novamente:

```bat
java -Djava.awt.headless=false -jar target\fidelidade-desktop-1.0.0.jar
```

Testes:

```bash
mvn test
```

## Evoluções recomendadas

Para produção, adicionar autenticação local do lojista, criptografia/backup do banco, LGPD (consentimento, finalidade, exclusão/exportação), auditoria e empacotamento nativo com `jpackage`.
