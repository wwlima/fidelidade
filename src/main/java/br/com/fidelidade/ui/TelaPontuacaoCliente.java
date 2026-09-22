package br.com.fidelidade.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import br.com.fidelidade.domain.Campanha;
import br.com.fidelidade.domain.Cliente;
import br.com.fidelidade.domain.Produto;
import br.com.fidelidade.dto.PontuacaoClienteDTO;
import br.com.fidelidade.service.CampanhaService;
import br.com.fidelidade.service.ClienteService;
import br.com.fidelidade.service.PontuacaoClienteService;
import br.com.fidelidade.service.ProdutoService;

public class TelaPontuacaoCliente extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final CampanhaService campanhaService;
    private final PontuacaoClienteService pontuacaoClienteService;

    private JFrame frameConsulta;
    private final Cliente[] clienteSelecionado = new Cliente[1];
    private final Produto[] produtoSelecionado = new Produto[1];
    private final Campanha[] campanhaSelecionada = new Campanha[1];

    private ComboBuscaPanel painelBuscaCliente;
    private ComboBuscaPanel painelBuscaProduto;
    private ComboBuscaPanel painelBuscaCampanha;

    private JPanel painelHistorico;
    private DefaultTableModel modeloHistorico;
    private JTextField txtQuantidade;
    private JButton btnPontuar;

    public TelaPontuacaoCliente(ClienteService clienteService, ProdutoService produtoService,
            CampanhaService campanhaService, PontuacaoClienteService pontuacaoClienteService) {
        this.clienteService = clienteService;
        this.produtoService = produtoService;
        this.campanhaService = campanhaService;
        this.pontuacaoClienteService = pontuacaoClienteService;

        inicializarComponentes();
    }

    /**
     * @wbp.parser.entryPoint
     */
    private void inicializarComponentes() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        add(criarPainelSelecao());
        add(Box.createVerticalStrut(12));
        add(new JSeparator());
        add(Box.createVerticalStrut(8));
        add(criarPainelHistorico());
        add(criarPainelAcoes());

        configurarBuscas();
        configurarSelecoes();
        configurarAcoes();
    }

    // ---------- Construção visual ----------

    private JPanel criarPainelSelecao() {
        var painelSelecao = new JPanel();
        painelSelecao.setLayout(new BoxLayout(painelSelecao, BoxLayout.Y_AXIS));
        painelSelecao.setBorder(BorderFactory.createTitledBorder("Seleção"));

        painelBuscaCliente = new ComboBuscaPanel("Cliente",
                new String[] { "ID", "Nome", "E-mail" }, new Dimension(600, 250));

        painelBuscaProduto = new ComboBuscaPanel("Produto",
                new String[] { "ID", "Nome" }, new Dimension(300, 150));
        painelBuscaProduto.habilitar(false);

        painelBuscaCampanha = new ComboBuscaPanel("Campanha",
                new String[] { "ID", "Nome", "Início", "Fim", "Ativa" }, new Dimension(300, 150));
        painelBuscaCampanha.habilitar(false);

        painelSelecao.add(painelBuscaCliente);
        painelSelecao.add(Box.createVerticalStrut(10));
        painelSelecao.add(painelBuscaProduto);
        painelSelecao.add(Box.createVerticalStrut(10));
        painelSelecao.add(painelBuscaCampanha);

        return painelSelecao;
    }

    private JPanel criarPainelHistorico() {
        painelHistorico = new JPanel(new BorderLayout(8, 8));
        painelHistorico.setBorder(BorderFactory.createTitledBorder("Cadastros da pontuação"));
        painelHistorico.setVisible(false);

        modeloHistorico = criarModeloTabela(new String[] { "Cliente", "Produto", "Campanha", "Quantidade", "Data" });
        var tabelaHistorico = new JTable(modeloHistorico);
        tabelaHistorico.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaHistorico.setFillsViewportHeight(true);
        var scrollHistorico = new JScrollPane(tabelaHistorico);

        painelHistorico.add(scrollHistorico, BorderLayout.CENTER);
        painelHistorico.add(criarPainelQuantidade(), BorderLayout.SOUTH);

        return painelHistorico;
    }

    private JPanel criarPainelQuantidade() {
        var painelQuantidade = new JPanel(new BorderLayout(8, 8));
        txtQuantidade = new JTextField();
        txtQuantidade.setEditable(false);
        txtQuantidade.setBackground(new Color(245, 245, 245));
        txtQuantidade.setHorizontalAlignment(JTextField.RIGHT);

        painelQuantidade.add(new JLabel("Quantidade de pontos"), BorderLayout.WEST);
        painelQuantidade.add(txtQuantidade, BorderLayout.CENTER);
        return painelQuantidade;
    }

    private JPanel criarPainelAcoes() {
        var painelAcoes = new JPanel(new BorderLayout(8, 8));
        painelAcoes.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        painelAcoes.add(new JSeparator(), BorderLayout.NORTH);

        var painelBotao = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnPontuar = new JButton("Pontuar");
        btnPontuar.setEnabled(false);
        var btnLimpar = new JButton("Limpar");

        painelBotao.add(btnPontuar);
        painelBotao.add(btnLimpar);
        painelAcoes.add(painelBotao, BorderLayout.CENTER);

        btnLimpar.addActionListener(_ -> limparTudo());

        return painelAcoes;
    }

    // ---------- Configuração de eventos ----------

    private void configurarBuscas() {
        painelBuscaCliente.configurarBusca(this::carregarClientes);
        painelBuscaProduto.configurarBusca(this::carregarProdutos);
        painelBuscaCampanha.configurarBusca(this::carregarCampanhas);
    }

    private void configurarSelecoes() {
        painelBuscaCliente.configurarSelecao(this::selecionarCliente);
        painelBuscaProduto.configurarSelecao(this::selecionarProduto);
        painelBuscaCampanha.configurarSelecao(this::selecionarCampanha);
    }

    private void configurarAcoes() {
        btnPontuar.addActionListener(_ -> pontuar());
    }

    // ---------- Carregamento das buscas ----------

    private void carregarClientes() {
        var modelo = painelBuscaCliente.getModelo();
        modelo.setRowCount(0);

        var texto = painelBuscaCliente.getTextoDigitado();
        if (texto.length() < 3) {
            painelBuscaCliente.esconderPopup();
            return;
        }

        clienteService.buscarPorNome(texto).stream().limit(10)
                .forEach(c -> modelo.addRow(new Object[] { c.getId(), c.getNome(), c.getEmail() }));

        painelBuscaCliente.mostrarPopup();
    }

    private void carregarProdutos() {
        var modelo = painelBuscaProduto.getModelo();
        modelo.setRowCount(0);

        var texto = painelBuscaProduto.getTextoDigitado();
        if (texto.isEmpty()) {
            painelBuscaProduto.esconderPopup();
            return;
        }

        produtoService.buscarPorNome(texto).stream().limit(10)
                .forEach(p -> modelo.addRow(new Object[] { p.getId(), p.getNome() }));

        painelBuscaProduto.mostrarPopup();
    }

    private void carregarCampanhas() {
        var modelo = painelBuscaCampanha.getModelo();
        modelo.setRowCount(0);

        var texto = painelBuscaCampanha.getTextoDigitado();
        if (texto.isEmpty()) {
            painelBuscaCampanha.esconderPopup();
            return;
        }

        campanhaService.buscarPorNomePaginado(texto, 0, 10).forEach(c -> modelo.addRow(new Object[] {
                c.getId(), c.getNome(),
                c.getDataInicioVigencia().format(FORMATO_DATA),
                c.getDataFimVigencia().format(FORMATO_DATA),
                c.isAtivo() ? "Sim" : "Não"
        }));

        painelBuscaCampanha.mostrarPopup();
    }

    // ---------- Seleção nas tabelas ----------

    private void selecionarCliente(int linha) {
        Long id = ((Number) painelBuscaCliente.getModelo().getValueAt(linha, 0)).longValue();
        clienteSelecionado[0] = clienteService.buscarPorId(id);

        if (clienteSelecionado[0] != null) {
            painelBuscaCliente.selecionarTexto(clienteSelecionado[0].getNome());
            painelBuscaProduto.habilitar(true);
            painelBuscaProduto.limpar();
            painelBuscaCampanha.habilitar(false);
            painelBuscaCampanha.limpar();
            painelBuscaProduto.focar();
        }

        produtoSelecionado[0] = null;
        campanhaSelecionada[0] = null;
        atualizarHistorico();
    }

    private void selecionarProduto(int linha) {
        Long id = ((Number) painelBuscaProduto.getModelo().getValueAt(linha, 0)).longValue();
        produtoSelecionado[0] = produtoService.buscarPorId(id).orElse(null);

        if (produtoSelecionado[0] != null) {
            painelBuscaProduto.selecionarTexto(produtoSelecionado[0].getNome());
            painelBuscaCampanha.habilitar(true);
            painelBuscaCampanha.limpar();
            painelBuscaCampanha.focar();
        }

        campanhaSelecionada[0] = null;
        atualizarHistorico();
    }

    private void selecionarCampanha(int linha) {
        Long id = ((Number) painelBuscaCampanha.getModelo().getValueAt(linha, 0)).longValue();
        campanhaSelecionada[0] = campanhaService.buscarPorId(id).orElse(null);

        if (campanhaSelecionada[0] != null) {
            painelBuscaCampanha.selecionarTexto(campanhaSelecionada[0].getNome());
        }

        atualizarHistorico();
    }

    // ---------- Histórico e pontuação ----------

    private void atualizarHistorico() {
        if (clienteSelecionado[0] == null) {
            painelHistorico.setVisible(false);
            modeloHistorico.setRowCount(0);
            txtQuantidade.setText("");
            btnPontuar.setEnabled(false);
            return;
        }

        List<PontuacaoClienteDTO> cadastros = pontuacaoClienteService.listarPorCliente(clienteSelecionado[0].getId());

        List<PontuacaoClienteDTO> filtrados = cadastros.stream()
                .filter(p -> produtoSelecionado[0] == null || p.produtoNome().equals(produtoSelecionado[0].getNome()))
                .filter(p -> campanhaSelecionada[0] == null || p.campanhaNome().equals(campanhaSelecionada[0].getNome()))
                .toList();

        painelHistorico.setVisible(true);
        modeloHistorico.setRowCount(0);
        filtrados.forEach(registro -> modeloHistorico.addRow(new Object[] {
                registro.clienteNome(), registro.produtoNome(), registro.campanhaNome(), registro.quantidade(),
                registro.dataCadastro() != null ? registro.dataCadastro().toLocalDate().format(FORMATO_DATA) : ""
        }));

        if (produtoSelecionado[0] != null && campanhaSelecionada[0] != null) {
            var registroAtual = pontuacaoClienteService.buscarPorClienteProdutoCampanha(
                    clienteSelecionado[0].getId(), produtoSelecionado[0].getId(), campanhaSelecionada[0].getId())
                    .orElse(null);

            txtQuantidade.setText(registroAtual != null ? String.valueOf(registroAtual.quantidade()) : "0");
        } else {
            txtQuantidade.setText("");
        }

        btnPontuar.setEnabled(produtoSelecionado[0] != null && campanhaSelecionada[0] != null);
    }

    private void pontuar() {
        if (clienteSelecionado[0] == null || produtoSelecionado[0] == null || campanhaSelecionada[0] == null) {
            JOptionPane.showMessageDialog(frameConsulta, "Selecione cliente, produto e campanha antes de pontuar.");
            return;
        }

        try {
            var registro = pontuacaoClienteService.pontuar(clienteSelecionado[0].getId(),
                    produtoSelecionado[0].getId(), campanhaSelecionada[0].getId());
            JOptionPane.showMessageDialog(frameConsulta,
                    "Pontos registrados com sucesso! Quantidade atual: " + registro.quantidade());
            atualizarHistorico();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frameConsulta, "Erro ao pontuar: " + ex.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparTudo() {
        clienteSelecionado[0] = null;
        produtoSelecionado[0] = null;
        campanhaSelecionada[0] = null;

        painelBuscaCliente.limpar();
        painelBuscaProduto.limpar();
        painelBuscaProduto.habilitar(false);
        painelBuscaCampanha.limpar();
        painelBuscaCampanha.habilitar(false);

        painelHistorico.setVisible(false);
        modeloHistorico.setRowCount(0);
        txtQuantidade.setText("");
        btnPontuar.setEnabled(false);

        painelBuscaCliente.focar();
    }

    private DefaultTableModel criarModeloTabela(String[] colunas) {
        return new DefaultTableModel(colunas, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    public void abrirTelaConsulta(JFrame frameConsulta) {
        this.frameConsulta = frameConsulta;
        limparTudo();
        frameConsulta.setTitle("Fidelidade — Pontuação de clientes");
        frameConsulta.getContentPane().removeAll();
        frameConsulta.setLayout(new BorderLayout(12, 12));
        frameConsulta.add(this, BorderLayout.CENTER);
        frameConsulta.revalidate();
        frameConsulta.repaint();
    }
}