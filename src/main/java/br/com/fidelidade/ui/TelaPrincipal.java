package br.com.fidelidade.ui;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import br.com.fidelidade.service.ClienteService;
import br.com.fidelidade.service.ProdutoService;

@Component
public class TelaPrincipal {
    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final TelaClientes telaClientes;
    private final TelaProdutos telaProdutos;
    private JFrame frameConsulta;

    public TelaPrincipal(ClienteService clienteService, ProdutoService produtoService) {
        this.clienteService = clienteService;
        this.produtoService = produtoService;
        this.telaClientes = new TelaClientes(clienteService);
        this.telaProdutos = new TelaProdutos(produtoService);
    }

    /**
     * A janela só é criada depois que o contexto Spring terminou de iniciar. A
     * criação também é garantida na Event Dispatch Thread do Swing.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void abrirAoIniciar() {
        SwingUtilities.invokeLater(() -> {
            if (GraphicsEnvironment.isHeadless()) {
                System.err.println("Não foi possível abrir a tela Swing: o ambiente gráfico está indisponível.");
                System.err.println("Propriedade java.awt.headless=" + System.getProperty("java.awt.headless"));
                System.err.println(
                        "Execute em uma sessão do Windows com área de trabalho, sem -Djava.awt.headless=true.");
                return;
            }
            criarJanelaConsulta();
        });
    }

    private void criarJanelaConsulta() {
        frameConsulta = new JFrame("Fidelidade");
        frameConsulta.setSize(950, 600);
        frameConsulta.setLocationRelativeTo(null);
        frameConsulta.setLayout(new BorderLayout(8, 8));
        frameConsulta.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameConsulta.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent evento) {
                br.com.fidelidade.FidelidadeApplication.encerrar();
            }
        });

        // Criar menu
        var menuBar = new JMenuBar();
        var menuGestao = new JMenu("Gestão");

        var itemClientes = new JMenuItem("Clientes");
        itemClientes.addActionListener(_ -> telaClientes.abrirTelaConsulta(frameConsulta));

        var itemProdutos = new JMenuItem("Produtos");
        itemProdutos.addActionListener(_ -> telaProdutos.abrirTelaConsulta(frameConsulta));

        menuGestao.add(itemClientes);
        menuGestao.add(itemProdutos);
        menuBar.add(menuGestao);

        frameConsulta.setJMenuBar(menuBar);
        frameConsulta.setVisible(true);

        // A tela inicial fica vazia até o usuário escolher uma opção do menu.
        frameConsulta.getContentPane().removeAll();
        frameConsulta.revalidate();
        frameConsulta.repaint();
    }
}