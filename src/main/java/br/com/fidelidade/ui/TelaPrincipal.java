package br.com.fidelidade.ui;

import br.com.fidelidade.service.ClienteService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
		frameConsulta.setSize(900, 550);
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
			int tipoFiltro = 0; // 0 = nenhum, 1 = nome, 2 = email, 3 = data
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

		// Painel superior com botão de cadastro
		var painelTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
		painelTop.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
		var btnCadastrar = new JButton("+ Cadastrar novo cliente");
		btnCadastrar.addActionListener(e -> abrirDialogCadastro(frameConsulta, carregarPagina, estadoPaginacao));
		painelTop.add(btnCadastrar);

		// Painel de filtro
		var painelFiltro = new JPanel(new GridLayout(2, 3, 8, 8));
		painelFiltro.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
		
		var campoBuscaNome = new JTextField();
		painelFiltro.add(new JLabel("Buscar por nome:"));
		painelFiltro.add(campoBuscaNome);
		
		var campoBuscaEmail = new JTextField();
		painelFiltro.add(new JLabel("Buscar por e-mail:"));
		painelFiltro.add(campoBuscaEmail);
		
		painelFiltro.add(new JLabel(""));
		painelFiltro.add(new JLabel(""));

		// Painel de tamanho de página
		var painelTamanho = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
		painelTamanho.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
		painelTamanho.add(new JLabel("Registros por página:"));
		
		var tamanhoComboBox = new JComboBox<>(new Integer[] { 2, 10, 50 });
		tamanhoComboBox.setSelectedItem(10);
		tamanhoComboBox.addActionListener(e -> {
			estadoPaginacao.tamanho = (Integer) tamanhoComboBox.getSelectedItem();
			estadoPaginacao.paginaAtual = 0;
			carregarPagina.accept(null);
		});
		painelTamanho.add(tamanhoComboBox);

		// Painel de botões de busca
		var painelBuscaBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
		
		var buscaNome = new JButton("Buscar por Nome");
		buscaNome.addActionListener(e -> {
			estadoPaginacao.filtroNome = campoBuscaNome.getText();
			estadoPaginacao.filtroEmail = "";
			estadoPaginacao.filtroData = null;
			estadoPaginacao.tipoFiltro = campoBuscaNome.getText().isEmpty() ? 0 : 1;
			estadoPaginacao.paginaAtual = 0;
			carregarPagina.accept(null);
		});

		var buscaEmail = new JButton("Buscar por E-mail");
		buscaEmail.addActionListener(e -> {
			estadoPaginacao.filtroNome = "";
			estadoPaginacao.filtroEmail = campoBuscaEmail.getText();
			estadoPaginacao.filtroData = null;
			estadoPaginacao.tipoFiltro = campoBuscaEmail.getText().isEmpty() ? 0 : 2;
			estadoPaginacao.paginaAtual = 0;
			carregarPagina.accept(null);
		});

		var buscaData = new JButton("Buscar por Data");
		buscaData.addActionListener(e -> {
			var dataStr = JOptionPane.showInputDialog(frameConsulta, "Data (dd/MM/yyyy):");
			if (dataStr != null && !dataStr.isEmpty()) {
				try {
					estadoPaginacao.filtroData = java.time.LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
					estadoPaginacao.filtroNome = "";
					estadoPaginacao.filtroEmail = "";
					estadoPaginacao.tipoFiltro = 3;
					estadoPaginacao.paginaAtual = 0;
					carregarPagina.accept(null);
				} catch (DateTimeParseException ex) {
					JOptionPane.showMessageDialog(frameConsulta, "Data inválida! Use o formato dd/MM/yyyy");
				}
			}
		});

		var limpar = new JButton("Limpar Filtros");
		limpar.addActionListener(e -> {
			estadoPaginacao.filtroNome = "";
			estadoPaginacao.filtroEmail = "";
			estadoPaginacao.filtroData = null;
			estadoPaginacao.tipoFiltro = 0;
			estadoPaginacao.paginaAtual = 0;
			campoBuscaNome.setText("");
			campoBuscaEmail.setText("");
			carregarPagina.accept(null);
		});

		painelBuscaBotoes.add(buscaNome);
		painelBuscaBotoes.add(buscaEmail);
		painelBuscaBotoes.add(buscaData);
		painelBuscaBotoes.add(limpar);

		// Painel de controles de paginação
		var painelPaginacao = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
		painelPaginacao.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
		
		var btnPrimeira = new JButton("« Primeira");
		btnPrimeira.addActionListener(e -> {
			estadoPaginacao.paginaAtual = 0;
			carregarPagina.accept(null);
		});
		
		var btnAnterior = new JButton("‹ Anterior");
		btnAnterior.addActionListener(e -> {
			if (estadoPaginacao.paginaAtual > 0) {
				estadoPaginacao.paginaAtual--;
				carregarPagina.accept(null);
			}
		});
		
		var btnProxima = new JButton("Próxima ›");
		btnProxima.addActionListener(e -> {
			estadoPaginacao.paginaAtual++;
			carregarPagina.accept(null);
		});
		
		var btnUltima = new JButton("Última »");
		btnUltima.addActionListener(e -> {
			var pagina = service.listarPaginado(0, estadoPaginacao.tamanho);
			if (estadoPaginacao.tipoFiltro == 1 && !estadoPaginacao.filtroNome.isEmpty()) {
				pagina = service.buscarPorNomePaginado(estadoPaginacao.filtroNome, 0, estadoPaginacao.tamanho);
			} else if (estadoPaginacao.tipoFiltro == 2 && !estadoPaginacao.filtroEmail.isEmpty()) {
				pagina = service.buscarPorEmailPaginado(estadoPaginacao.filtroEmail, 0, estadoPaginacao.tamanho);
			} else if (estadoPaginacao.tipoFiltro == 3 && estadoPaginacao.filtroData != null) {
				pagina = service.buscarPorDataNascimentoPaginado(estadoPaginacao.filtroData, 0, estadoPaginacao.tamanho);
			}
			estadoPaginacao.paginaAtual = pagina.getTotalPages() - 1;
			carregarPagina.accept(null);
		});
		
		painelPaginacao.add(btnPrimeira);
		painelPaginacao.add(btnAnterior);
		painelPaginacao.add(infoPaginacao);
		painelPaginacao.add(btnProxima);
		painelPaginacao.add(btnUltima);

		frameConsulta.add(painelTop, BorderLayout.PAGE_START);
		frameConsulta.add(painelFiltro, BorderLayout.NORTH);
		frameConsulta.add(painelTamanho, BorderLayout.BEFORE_FIRST_LINE);
		frameConsulta.add(painelBuscaBotoes, BorderLayout.BEFORE_LINE_BEGINS);
		frameConsulta.add(scrollTabela, BorderLayout.CENTER);
		frameConsulta.add(painelPaginacao, BorderLayout.SOUTH);
		
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
		btnCadastrar.addActionListener(e -> {
			try {
				LocalDate data = null;
				if (dataNascimento.getText() != null && !dataNascimento.getText().isEmpty() && !dataNascimento.getText().equals("dd/MM/yyyy")) {
					try {
						data = LocalDate.parse(dataNascimento.getText(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
					} catch (DateTimeParseException ex) {
						throw new IllegalArgumentException("Data de nascimento inválida. Use o formato dd/MM/yyyy");
					}
				}
				var cliente = service.cadastrar(nome.getText(), email.getText(), telefone.getText(), data);
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
		btnCancelar.addActionListener(e -> dialog.dispose());

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
