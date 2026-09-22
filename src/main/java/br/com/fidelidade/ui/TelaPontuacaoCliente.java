package br.com.fidelidade.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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

	private final ClienteService clienteService;
	private final ProdutoService produtoService;
	private final CampanhaService campanhaService;
	private final PontuacaoClienteService pontuacaoClienteService;

	private JFrame frameConsulta;
	private Cliente[] clienteSelecionado;
	private Produto[] produtoSelecionado;
	private Campanha[] campanhaSelecionada;
	
    // Componentes que precisam ser limpos na abertura da tela
    private JComboBox<String> comboCliente;
    private JComboBox<String> comboProduto;
    private JComboBox<String> comboCampanha;
    private JTextField txtCliente;
    private JTextField txtProduto;
    private JTextField txtCampanha;
    private JPopupMenu popupClientes;
    private JPopupMenu popupProdutos;
    private JPopupMenu popupCampanhas;
    private DefaultTableModel modeloCliente;
    private DefaultTableModel modeloProduto;
    private DefaultTableModel modeloCampanha;
    private DefaultTableModel modeloHistorico;
    private JPanel painelHistorico;
    private JTextField txtQuantidade;
    private JButton btnPontuar;

	public TelaPontuacaoCliente(ClienteService clienteService, ProdutoService produtoService,
			CampanhaService campanhaService, PontuacaoClienteService pontuacaoClienteService) {
		this.clienteService = clienteService;
		this.produtoService = produtoService;
		this.campanhaService = campanhaService;
		this.pontuacaoClienteService = pontuacaoClienteService;

		clienteSelecionado = new Cliente[1];
		produtoSelecionado = new Produto[1];
		campanhaSelecionada = new Campanha[1];

		inicializarComponentes();
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	private void inicializarComponentes() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

		var painelSelecao = new JPanel();
		painelSelecao.setLayout(new BoxLayout(painelSelecao, BoxLayout.Y_AXIS));
		painelSelecao.setBorder(BorderFactory.createTitledBorder("Seleção"));

		var painelCliente = new JPanel(new BorderLayout(4, 4));
		painelCliente.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		var lblCliente = new JLabel("Cliente");
		comboCliente = new JComboBox<String>();
		comboCliente.setEditable(true);
		comboCliente.setPrototypeDisplayValue("Cliente muito longo para teste");
		txtCliente = (JTextField) comboCliente.getEditor().getEditorComponent();

		modeloCliente = criarModeloTabela(new String[] { "ID", "Nome", "E-mail" });
		var tabelaClientes = new JTable(modeloCliente);
		tabelaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabelaClientes.setFillsViewportHeight(true);
		var scrollClientes = new JScrollPane(tabelaClientes);
		scrollClientes.setPreferredSize(new java.awt.Dimension(600, 250));

		popupClientes = new JPopupMenu();
		popupClientes.add(scrollClientes);

		painelCliente.add(lblCliente, BorderLayout.NORTH);
		painelCliente.add(comboCliente, BorderLayout.CENTER);

		var painelProduto = new JPanel(new BorderLayout(8, 8));
		painelProduto.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		var lblProduto = new JLabel("Produto");
		comboProduto = new JComboBox<String>();
		comboProduto.setEditable(true);
		comboProduto.setEnabled(false);
		txtProduto = (JTextField) comboProduto.getEditor().getEditorComponent();

		modeloProduto = criarModeloTabela(new String[] { "ID", "Nome" });
		var tabelaProdutos = new JTable(modeloProduto);
		tabelaProdutos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabelaProdutos.setFillsViewportHeight(true);
		var scrollProdutos = new JScrollPane(tabelaProdutos);
		scrollProdutos.setPreferredSize(new java.awt.Dimension(300, 150));

		popupProdutos = new JPopupMenu();
		popupProdutos.add(scrollProdutos);

		painelProduto.add(lblProduto, BorderLayout.NORTH);
		painelProduto.add(comboProduto, BorderLayout.CENTER);

		var painelCampanha = new JPanel(new BorderLayout(8, 8));
		painelCampanha.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		var lblCampanha = new JLabel("Campanha");
		comboCampanha = new JComboBox<String>();
		comboCampanha.setEditable(true);
		comboCampanha.setEnabled(false);
		txtCampanha = (JTextField) comboCampanha.getEditor().getEditorComponent();

		modeloCampanha = criarModeloTabela(new String[] { "ID", "Nome", "Início", "Fim", "Ativa" });
		var tabelaCampanhas = new JTable(modeloCampanha);
		tabelaCampanhas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabelaCampanhas.setFillsViewportHeight(true);
		var scrollCampanhas = new JScrollPane(tabelaCampanhas);
		scrollCampanhas.setPreferredSize(new java.awt.Dimension(300, 150));

		popupCampanhas = new JPopupMenu();
		popupCampanhas.add(scrollCampanhas);

		painelCampanha.add(lblCampanha, BorderLayout.NORTH);
		painelCampanha.add(comboCampanha, BorderLayout.CENTER);

		painelSelecao.add(painelCliente);
		painelSelecao.add(Box.createVerticalStrut(10));
		painelSelecao.add(painelProduto);
		painelSelecao.add(Box.createVerticalStrut(10));
		painelSelecao.add(painelCampanha);

		add(painelSelecao);
		add(Box.createVerticalStrut(12));
		add(new JSeparator());
		add(Box.createVerticalStrut(8));

		painelHistorico = new JPanel(new BorderLayout(8, 8));
		painelHistorico.setBorder(BorderFactory.createTitledBorder("Cadastros da pontuação"));
		painelHistorico.setVisible(false);

		modeloHistorico = criarModeloTabela(
				new String[] { "Cliente", "Produto", "Campanha", "Quantidade", "Data" });
		var tabelaHistorico = new JTable(modeloHistorico);
		tabelaHistorico.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabelaHistorico.setFillsViewportHeight(true);
		var scrollHistorico = new JScrollPane(tabelaHistorico);
		
		var painelQuantidade = new JPanel(new BorderLayout(8, 8));
        txtQuantidade = new JTextField();
        txtQuantidade.setEditable(false);
        txtQuantidade.setBackground(new Color(245, 245, 245));
        txtQuantidade.setHorizontalAlignment(JTextField.RIGHT);
        
        painelQuantidade.add(new JLabel("Quantidade de pontos"), BorderLayout.WEST);
        painelQuantidade.add(txtQuantidade, BorderLayout.CENTER);

		painelHistorico.add(scrollHistorico, BorderLayout.CENTER);
		painelHistorico.add(painelQuantidade, BorderLayout.SOUTH);

		add(painelHistorico);

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
		add(painelAcoes);

		Runnable atualizarHistorico = () -> {
			if (clienteSelecionado[0] == null) {
				painelHistorico.setVisible(false);
		        modeloHistorico.setRowCount(0);
		        txtQuantidade.setText("");
		        btnPontuar.setEnabled(false);
				return;
			}

			List<PontuacaoClienteDTO> cadastros = pontuacaoClienteService.listarPorCliente(clienteSelecionado[0].getId());

			List<PontuacaoClienteDTO> filtrados = cadastros.stream().filter(
					p -> produtoSelecionado[0] == null || p.produtoNome().equals(produtoSelecionado[0].getNome()))
					.filter(p -> campanhaSelecionada[0] == null
							|| p.campanhaNome().equals(campanhaSelecionada[0].getNome()))
					.toList();

			painelHistorico.setVisible(true);
			modeloHistorico.setRowCount(0);
			filtrados
					.forEach(registro -> modeloHistorico.addRow(new Object[] { registro.clienteNome(),
							registro.produtoNome(), registro.campanhaNome(), registro.quantidade(),
							registro.dataCadastro() != null
									? registro.dataCadastro().toLocalDate().format(
											java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
									: "" }));

			 // Só consulta a quantidade exata quando produto E campanha já estiverem selecionados
		    if (produtoSelecionado[0] != null && campanhaSelecionada[0] != null) {
		        var registroAtual = pontuacaoClienteService.buscarPorClienteProdutoCampanha(
		                clienteSelecionado[0].getId(),
		                produtoSelecionado[0].getId(),
		                campanhaSelecionada[0].getId()).orElse(null);

		        txtQuantidade.setText(registroAtual != null ? String.valueOf(registroAtual.quantidade()) : "0");
		    } else {
		        txtQuantidade.setText("");
		    }

		    btnPontuar.setEnabled(produtoSelecionado[0] != null && campanhaSelecionada[0] != null);
		};

		Runnable carregarClientes = () -> {
			modeloCliente.setRowCount(0);
			var texto = txtCliente.getText() == null ? "" : txtCliente.getText().trim();
			if (texto.isEmpty() || texto.length() < 3) {
				popupClientes.setVisible(false);
				return;
			}

			clienteService.buscarPorNome(texto).stream().limit(10)
					.forEach(c -> modeloCliente.addRow(new Object[] { c.getId(), c.getNome(), c.getEmail() }));

			if (modeloCliente.getRowCount() > 0) {
				popupClientes.show(comboCliente, 0, comboCliente.getHeight());
			} else {
				popupClientes.setVisible(false);
			}
		};

		Runnable carregarProdutos = () -> {
			modeloProduto.setRowCount(0);
			var texto = txtProduto.getText() == null ? "" : txtProduto.getText().trim();
			if (texto.isEmpty()) {
				popupProdutos.setVisible(false);
				return;
			}

			produtoService.buscarPorNome(texto).stream().limit(10)
					.forEach(p -> modeloProduto.addRow(new Object[] { p.getId(), p.getNome() }));

			if (modeloProduto.getRowCount() > 0) {
				popupProdutos.show(comboProduto, 0, comboProduto.getHeight());
			} else {
				popupProdutos.setVisible(false);
			}
		};

		Runnable carregarCampanhas = () -> {
			modeloCampanha.setRowCount(0);
			var texto = txtCampanha.getText() == null ? "" : txtCampanha.getText().trim();
			if (texto.isEmpty()) {
				popupCampanhas.setVisible(false);
				return;
			}

			campanhaService.buscarPorNomePaginado(texto, 0, 10)
					.forEach(c -> modeloCampanha.addRow(new Object[] { c.getId(), c.getNome(),
							c.getDataInicioVigencia()
									.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
							c.getDataFimVigencia().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
							c.isAtivo() ? "Sim" : "Não" }));

			if (modeloCampanha.getRowCount() > 0) {
				popupCampanhas.show(comboCampanha, 0, comboCampanha.getHeight());
			} else {
				popupCampanhas.setVisible(false);
			}
		};

		txtCliente.getDocument().addDocumentListener(new DocumentListener() {
			private void onChange() {
				SwingUtilities.invokeLater(carregarClientes);
				txtCliente.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				onChange();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				onChange();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				onChange();
			}
		});

		txtProduto.getDocument().addDocumentListener(new DocumentListener() {
			private void onChange() {
				SwingUtilities.invokeLater(carregarProdutos);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				onChange();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				onChange();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				onChange();
			}
		});

		txtCampanha.getDocument().addDocumentListener(new DocumentListener() {
			private void onChange() {
				SwingUtilities.invokeLater(carregarCampanhas);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				onChange();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				onChange();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				onChange();
			}
		});

		tabelaClientes.getSelectionModel().addListSelectionListener(_ -> {
			if (tabelaClientes.getSelectedRow() < 0) {
				return;
			}
			Long id = ((Number) modeloCliente.getValueAt(tabelaClientes.getSelectedRow(), 0)).longValue();
			clienteSelecionado[0] = clienteService.buscarPorId(id);
			if (clienteSelecionado[0] != null) {
				comboCliente.setSelectedItem(clienteSelecionado[0].getNome());
				popupClientes.setVisible(false);
				comboProduto.setEnabled(true);
				comboProduto.setSelectedItem("");
				txtProduto.setText("");
				modeloProduto.setRowCount(0);
				popupProdutos.setVisible(false);
				comboCampanha.setEnabled(false);
				comboCampanha.setSelectedItem("");
				txtCampanha.setText("");
				modeloCampanha.setRowCount(0);
				popupCampanhas.setVisible(false);
				comboProduto.requestFocusInWindow();
			}
			produtoSelecionado[0] = null;
			campanhaSelecionada[0] = null;
			atualizarHistorico.run();
		});

		tabelaProdutos.getSelectionModel().addListSelectionListener(_ -> {
			if (tabelaProdutos.getSelectedRow() < 0) {
				return;
			}
			Long id = ((Number) modeloProduto.getValueAt(tabelaProdutos.getSelectedRow(), 0)).longValue();
			produtoSelecionado[0] = produtoService.buscarPorId(id).orElse(null);
			if (produtoSelecionado[0] != null) {
				comboProduto.setSelectedItem(produtoSelecionado[0].getNome());
				popupProdutos.setVisible(false);
				comboCampanha.setEnabled(true);
				comboCampanha.setSelectedItem("");
				txtCampanha.setText("");
				modeloCampanha.setRowCount(0);
				popupCampanhas.setVisible(false);
				comboCampanha.requestFocusInWindow();
			}
			campanhaSelecionada[0] = null;
			atualizarHistorico.run();
		});

		tabelaCampanhas.getSelectionModel().addListSelectionListener(_ -> {
			if (tabelaCampanhas.getSelectedRow() < 0) {
				return;
			}
			Long id = ((Number) modeloCampanha.getValueAt(tabelaCampanhas.getSelectedRow(), 0)).longValue();
			campanhaSelecionada[0] = campanhaService.buscarPorId(id).orElse(null);
			if (campanhaSelecionada[0] != null) {
				comboCampanha.setSelectedItem(campanhaSelecionada[0].getNome());
				popupCampanhas.setVisible(false);
			}
			atualizarHistorico.run();
		});

		btnPontuar.addActionListener(_ -> {
			if (clienteSelecionado[0] == null || produtoSelecionado[0] == null || campanhaSelecionada[0] == null) {
				JOptionPane.showMessageDialog(frameConsulta, "Selecione cliente, produto e campanha antes de pontuar.");
				return;
			}

			try {
				var registro = pontuacaoClienteService.pontuar(clienteSelecionado[0].getId(),
						produtoSelecionado[0].getId(), campanhaSelecionada[0].getId());
				JOptionPane.showMessageDialog(frameConsulta,
						"Pontos registrados com sucesso! Quantidade atual: " + registro.quantidade());
				atualizarHistorico.run();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(frameConsulta, "Erro ao pontuar: " + ex.getMessage(), "Erro",
						JOptionPane.ERROR_MESSAGE);
			}
		});
		
		btnLimpar.addActionListener(_ -> {
			limparTudo();
		});
		
		
	}

	private void limparTudo() {
	    clienteSelecionado[0] = null;
	    produtoSelecionado[0] = null;
	    campanhaSelecionada[0] = null;

	    comboCliente.setSelectedItem("");
	    txtCliente.setText("");
	    modeloCliente.setRowCount(0);
	    popupClientes.setVisible(false);

	    comboProduto.setSelectedItem("");
	    txtProduto.setText("");
	    modeloProduto.setRowCount(0);
	    popupProdutos.setVisible(false);
	    comboProduto.setEnabled(false);

	    comboCampanha.setSelectedItem("");
	    txtCampanha.setText("");
	    modeloCampanha.setRowCount(0);
	    popupCampanhas.setVisible(false);
	    comboCampanha.setEnabled(false);

	    painelHistorico.setVisible(false);
	    modeloHistorico.setRowCount(0);
	    txtQuantidade.setText("");

	    btnPontuar.setEnabled(false);

	    comboCliente.requestFocusInWindow();
	}
	
	
	private DefaultTableModel criarModeloTabela(String[] colunas) {
		return new DefaultTableModel(colunas, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
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