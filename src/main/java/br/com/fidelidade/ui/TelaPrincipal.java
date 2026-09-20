package br.com.fidelidade.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import br.com.fidelidade.service.ClienteService;

@Component
public class TelaPrincipal {
	private final ClienteService service;

	public TelaPrincipal(ClienteService service) {
		this.service = service;
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
			abrirTelaConsulta();
		});
	}

	private void abrirTelaConsulta() {
		var frameConsulta = new JFrame("Fidelidade — Clientes");
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

		// Estado da paginação
		var estadoPaginacao = new Object() {
			int paginaAtual = 0;
			int tamanho = 10;
			String filtroNome = "";
			String filtroEmail = "";
			java.time.LocalDate filtroData = null;
			int tipoFiltro = 0;
		};

		// Tabela de resultados
		var colunas = new String[] { "ID", "Nome", "E-mail", "Telefone", "Data de Nascimento", "Pontos" };
		var modeloTabela = new DefaultTableModel(colunas, 0) {
			@Override public boolean isCellEditable(int row, int column) { return false; }
		};
		var tabela = new JTable(modeloTabela);
		var scrollTabela = new JScrollPane(tabela);

		// Label para informações de paginação
		var infoPaginacao = new JLabel("Página 1 de 1");

		// Método para carregar página
		var carregarPagina = new java.util.function.Consumer<Object>() {
			@Override public void accept(Object ignored) {
				modeloTabela.setRowCount(0);
				var pagina = service.listarPaginado(estadoPaginacao.paginaAtual, estadoPaginacao.tamanho);
				
				if (estadoPaginacao.tipoFiltro == 1 && !estadoPaginacao.filtroNome.isEmpty()) {
					pagina = service.buscarPorNomePaginado(estadoPaginacao.filtroNome, estadoPaginacao.paginaAtual, estadoPaginacao.tamanho);
				} else if (estadoPaginacao.tipoFiltro == 2 && !estadoPaginacao.filtroEmail.isEmpty()) {
					pagina = service.buscarPorEmailPaginado(estadoPaginacao.filtroEmail, estadoPaginacao.paginaAtual, estadoPaginacao.tamanho);
				} else if (estadoPaginacao.tipoFiltro == 3 && estadoPaginacao.filtroData != null) {
					pagina = service.buscarPorDataNascimentoPaginado(estadoPaginacao.filtroData, estadoPaginacao.paginaAtual, estadoPaginacao.tamanho);
				}
				
				for (var cliente : pagina.getContent()) {
					modeloTabela.addRow(new Object[] { 
						cliente.getId(), 
						cliente.getNome(), 
						cliente.getEmail(), 
						cliente.getTelefone(),
						cliente.getDataNascimento() != null ? cliente.getDataNascimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "",
						cliente.getPontos()
					});
				}
				
				infoPaginacao.setText("Página " + (estadoPaginacao.paginaAtual + 1) + " de " + (pagina.getTotalPages() == 0 ? 1 : pagina.getTotalPages()));
			}
		};

		// Painel esquerdo com botões
		var painelEsquerdo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
		painelEsquerdo.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
		var btnCadastrar = new JButton("+ Cadastrar novo cliente");
		btnCadastrar.addActionListener(_ -> abrirDialogCadastro(frameConsulta, carregarPagina, estadoPaginacao));
		painelEsquerdo.add(btnCadastrar);
		
		painelEsquerdo.add(new JLabel("     Registros por página:"));
		var tamanhoComboBox = new JComboBox<>(new Integer[] { 2, 10, 50 });
		tamanhoComboBox.setSelectedItem(10);
		tamanhoComboBox.addActionListener(_ -> {
			estadoPaginacao.tamanho = (Integer) tamanhoComboBox.getSelectedItem();
			estadoPaginacao.paginaAtual = 0;
			carregarPagina.accept(null);
		});
		painelEsquerdo.add(tamanhoComboBox);

		// Painel de título
		var painelTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
		painelTitulo.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
		painelTitulo.setBackground(new java.awt.Color(240, 240, 240));
		var lblTitulo = new JLabel("Gestão de Clientes");
		lblTitulo.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
		painelTitulo.add(lblTitulo);

		// Painel de filtro/pesquisa
		var painelFiltro = new JPanel(new GridLayout(2, 4, 8, 8));
		painelFiltro.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
		
		var campoBuscaNome = new JTextField(15);
		painelFiltro.add(new JLabel("Nome:"));
		painelFiltro.add(campoBuscaNome);
		
		var campoBuscaEmail = new JTextField(15);
		painelFiltro.add(new JLabel("E-mail:"));
		painelFiltro.add(campoBuscaEmail);
		
		var campoBuscaData = new JTextField(15);
		campoBuscaData.setText("dd/MM/yyyy");
		painelFiltro.add(new JLabel("Data de Nascimento:"));
		painelFiltro.add(campoBuscaData);
		
		painelFiltro.add(new JLabel(""));
		painelFiltro.add(new JLabel(""));

		// Painel de botões de ação
		var painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
		painelBotoes.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));
		
		var btnPesquisar = new JButton("Pesquisar");
		btnPesquisar.addActionListener(_ -> {
			var temNome = campoBuscaNome.getText() != null && !campoBuscaNome.getText().trim().isEmpty();
			var temEmail = campoBuscaEmail.getText() != null && !campoBuscaEmail.getText().trim().isEmpty();
			var temData = campoBuscaData.getText() != null && !campoBuscaData.getText().trim().isEmpty() && !campoBuscaData.getText().equals("dd/MM/yyyy");
			
			estadoPaginacao.paginaAtual = 0;
			
			if (temNome) {
				estadoPaginacao.filtroNome = campoBuscaNome.getText().trim();
				estadoPaginacao.filtroEmail = "";
				estadoPaginacao.filtroData = null;
				estadoPaginacao.tipoFiltro = 1;
				carregarPagina.accept(null);
			} else if (temEmail) {
				estadoPaginacao.filtroNome = "";
				estadoPaginacao.filtroEmail = campoBuscaEmail.getText().trim();
				estadoPaginacao.filtroData = null;
				estadoPaginacao.tipoFiltro = 2;
				carregarPagina.accept(null);
			} else if (temData) {
				try {
					estadoPaginacao.filtroData = java.time.LocalDate.parse(campoBuscaData.getText(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
					estadoPaginacao.filtroNome = "";
					estadoPaginacao.filtroEmail = "";
					estadoPaginacao.tipoFiltro = 3;
					carregarPagina.accept(null);
				} catch (DateTimeParseException ex) {
					JOptionPane.showMessageDialog(frameConsulta, "Data inválida! Use o formato dd/MM/yyyy");
				}
			} else {
				estadoPaginacao.filtroNome = "";
				estadoPaginacao.filtroEmail = "";
				estadoPaginacao.filtroData = null;
				estadoPaginacao.tipoFiltro = 0;
				carregarPagina.accept(null);
			}
		});
		
		var btnLimpar = new JButton("Limpar");
		btnLimpar.addActionListener(_ -> {
			campoBuscaNome.setText("");
			campoBuscaEmail.setText("");
			campoBuscaData.setText("dd/MM/yyyy");
			estadoPaginacao.filtroNome = "";
			estadoPaginacao.filtroEmail = "";
			estadoPaginacao.filtroData = null;
			estadoPaginacao.tipoFiltro = 0;
			estadoPaginacao.paginaAtual = 0;
			carregarPagina.accept(null);
		});
		
		painelBotoes.add(btnPesquisar);
		painelBotoes.add(btnLimpar);

		// Painel superior que contém filtro e botões
		var painelBuscaSuperior = new JPanel(new BorderLayout(8, 8));
		painelBuscaSuperior.add(painelFiltro, BorderLayout.NORTH);
		painelBuscaSuperior.add(painelBotoes, BorderLayout.CENTER);

		// Painel de controles de paginação
		var painelPaginacao = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
		painelPaginacao.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
		
		var btnPrimeira = new JButton("« Primeira");
		btnPrimeira.addActionListener(_ -> {
			estadoPaginacao.paginaAtual = 0;
			carregarPagina.accept(null);
		});
		
		var btnAnterior = new JButton("‹ Anterior");
		btnAnterior.addActionListener(_ -> {
			if (estadoPaginacao.paginaAtual > 0) {
				estadoPaginacao.paginaAtual--;
				carregarPagina.accept(null);
			}
		});
		
		var btnProxima = new JButton("Próxima ›");
		btnProxima.addActionListener(_ -> {
			estadoPaginacao.paginaAtual++;
			carregarPagina.accept(null);
		});
		
		var btnUltima = new JButton("Última »");
		btnUltima.addActionListener(_ -> {
			var pagina = service.listarPaginado(0, estadoPaginacao.tamanho);
			if (estadoPaginacao.tipoFiltro == 1 && !estadoPaginacao.filtroNome.isEmpty()) {
				pagina = service.buscarPorNomePaginado(estadoPaginacao.filtroNome, 0, estadoPaginacao.tamanho);
			} else if (estadoPaginacao.tipoFiltro == 2 && !estadoPaginacao.filtroEmail.isEmpty()) {
				pagina = service.buscarPorEmailPaginado(estadoPaginacao.filtroEmail, 0, estadoPaginacao.tamanho);
			} else if (estadoPaginacao.tipoFiltro == 3 && estadoPaginacao.filtroData != null) {
				pagina = service.buscarPorDataNascimentoPaginado(estadoPaginacao.filtroData, 0, estadoPaginacao.tamanho);
			}
			estadoPaginacao.paginaAtual = Math.max(0, pagina.getTotalPages() - 1);
			carregarPagina.accept(null);
		});
		
		painelPaginacao.add(btnPrimeira);
		painelPaginacao.add(btnAnterior);
		painelPaginacao.add(infoPaginacao);
		painelPaginacao.add(btnProxima);
		painelPaginacao.add(btnUltima);

		// Painel inferior combinado: esquerdo + paginação
		var painelInferior = new JPanel(new BorderLayout(8, 8));
		painelInferior.add(painelEsquerdo, BorderLayout.WEST);
		painelInferior.add(painelPaginacao, BorderLayout.EAST);

		// Painel principal superior combinado: título + busca
		var painelPrincipalSuperior = new JPanel(new BorderLayout(8, 8));
		painelPrincipalSuperior.add(painelTitulo, BorderLayout.NORTH);
		painelPrincipalSuperior.add(painelBuscaSuperior, BorderLayout.CENTER);

		frameConsulta.add(painelPrincipalSuperior, BorderLayout.NORTH);
		frameConsulta.add(scrollTabela, BorderLayout.CENTER);
		frameConsulta.add(painelInferior, BorderLayout.SOUTH);
		
		// Carregar os primeiros 10 registros ao abrir
		carregarPagina.accept(null);
		
		frameConsulta.setVisible(true);
	}

	private void abrirDialogCadastro(JFrame parent, java.util.function.Consumer<Object> aposXadastro, Object estadoPaginacao) {
		var dialog = new JDialog(parent, "Cadastrar novo cliente", true);
		dialog.setSize(400, 300);
		dialog.setLocationRelativeTo(parent);
		dialog.setLayout(new BorderLayout(8, 8));

		var nome = new JTextField();
		var email = new JTextField();
		var telefone = new JTextField();
		var dataNascimento = new JTextField("dd/MM/yyyy");
		var status = new JLabel(" ");

		var form = new JPanel(new GridLayout(0, 2, 8, 8));
		form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
		form.add(new JLabel("Nome completo *"));
		form.add(nome);
		form.add(new JLabel("E-mail *"));
		form.add(email);
		form.add(new JLabel("Telefone"));
		form.add(telefone);
		form.add(new JLabel("Data de nascimento (dd/MM/yyyy)"));
		form.add(dataNascimento);

		var btnCadastrar = new JButton("Cadastrar");
		btnCadastrar.addActionListener(_ -> {
			try {
				LocalDate data = null;
				if (dataNascimento.getText() != null && !dataNascimento.getText().isEmpty() && !dataNascimento.getText().equals("dd/MM/yyyy")) {
					try {
						data = LocalDate.parse(dataNascimento.getText(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
					} catch (DateTimeParseException ex) {
						throw new IllegalArgumentException("Data de nascimento inválida. Use o formato dd/MM/yyyy");
					}
				}
				//var cliente = service.cadastrar(nome.getText(), email.getText(), telefone.getText(), data);
				status.setText("✓ Cliente cadastrado com sucesso!");
				nome.setText("");
				email.setText("");
				telefone.setText("");
				dataNascimento.setText("dd/MM/yyyy");
				// Atualizar lista após cadastro (volta para página 1)
				var pag = (Object) estadoPaginacao;
				java.lang.reflect.Field field = pag.getClass().getDeclaredField("paginaAtual");
				field.setAccessible(true);
				field.setInt(pag, 0);
				aposXadastro.accept(null);
				// Fechar dialog após 1 segundo
				new Thread(() -> {
					try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
					SwingUtilities.invokeLater(dialog::dispose);
				}).start();
			} catch (Exception ex) {
				status.setText("✗ Erro: " + ex.getMessage());
			}
		});

		var btnCancelar = new JButton("Cancelar");
		btnCancelar.addActionListener(_ -> dialog.dispose());

		var botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
		botoes.add(btnCadastrar);
		botoes.add(btnCancelar);

		var rodape = new JPanel(new BorderLayout(8, 8));
		rodape.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
		rodape.add(status, BorderLayout.WEST);
		rodape.add(botoes, BorderLayout.EAST);

		dialog.add(form, BorderLayout.NORTH);
		dialog.add(rodape, BorderLayout.SOUTH);
		dialog.setVisible(true);
	}
}
