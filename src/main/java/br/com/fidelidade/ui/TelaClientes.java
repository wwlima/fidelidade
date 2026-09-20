package br.com.fidelidade.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import br.com.fidelidade.domain.Cliente;
import br.com.fidelidade.service.ClienteService;

public class TelaClientes {
    private final ClienteService clienteService;

    public TelaClientes(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    public void abrirTelaConsulta(JFrame frameConsulta) {
        frameConsulta.setTitle("Fidelidade — Clientes");
        frameConsulta.getContentPane().removeAll();
        frameConsulta.setLayout(new BorderLayout(8, 8));

        var estadoPaginacao = new Object() {
            int paginaAtual = 0;
            int tamanho = 10;
            String filtroNome = "";
            String filtroEmail = "";
            LocalDate filtroData = null;
            int tipoFiltro = 0;
        };

        var colunas = new String[] { "ID", "Nome", "E-mail", "Telefone", "Data de Nascimento", "Pontos" };
        var modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        var tabela = new JTable(modeloTabela);
        var menuContexto = new JPopupMenu();
        var itemEditar = new JMenuItem("Editar cliente");
        var itemExcluir = new JMenuItem("Excluir cliente");
        menuContexto.add(itemEditar);
        menuContexto.add(itemExcluir);
        tabela.setComponentPopupMenu(menuContexto);

        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int linha = tabela.rowAtPoint(e.getPoint());
                    if (linha >= 0) {
                        tabela.setRowSelectionInterval(linha, linha);
                    }
                    menuContexto.show(tabela, e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int linha = tabela.rowAtPoint(e.getPoint());
                    if (linha >= 0) {
                        tabela.setRowSelectionInterval(linha, linha);
                    }
                    menuContexto.show(tabela, e.getX(), e.getY());
                }
            }
        });

        var scrollTabela = new JScrollPane(tabela);
        var infoPaginacao = new JLabel("Página 1 de 1");

        var carregarPagina = new java.util.function.Consumer<Object>() {
            @Override public void accept(Object ignored) {
                modeloTabela.setRowCount(0);
                var pagina = clienteService.listarPaginado(estadoPaginacao.paginaAtual, estadoPaginacao.tamanho);

                if (estadoPaginacao.tipoFiltro == 1 && !estadoPaginacao.filtroNome.isEmpty()) {
                    pagina = clienteService.buscarPorNomePaginado(estadoPaginacao.filtroNome, estadoPaginacao.paginaAtual, estadoPaginacao.tamanho);
                } else if (estadoPaginacao.tipoFiltro == 2 && !estadoPaginacao.filtroEmail.isEmpty()) {
                    pagina = clienteService.buscarPorEmailPaginado(estadoPaginacao.filtroEmail, estadoPaginacao.paginaAtual, estadoPaginacao.tamanho);
                } else if (estadoPaginacao.tipoFiltro == 3 && estadoPaginacao.filtroData != null) {
                    pagina = clienteService.buscarPorDataNascimentoPaginado(estadoPaginacao.filtroData, estadoPaginacao.paginaAtual, estadoPaginacao.tamanho);
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

        itemEditar.addActionListener(_ -> {
            int linha = tabela.getSelectedRow();
            if (linha < 0) {
                return;
            }
            Long id = (Long) modeloTabela.getValueAt(linha, 0);
            Cliente cliente = clienteService.buscarPorId(id);
            if (cliente != null) {
                abrirDialogCliente(frameConsulta, cliente, carregarPagina, true);
            }
        });

        itemExcluir.addActionListener(_ -> {
            int linha = tabela.getSelectedRow();
            if (linha < 0) {
                return;
            }
            Long id = (Long) modeloTabela.getValueAt(linha, 0);
            Cliente cliente = clienteService.buscarPorId(id);
            if (cliente == null) {
                return;
            }

            String dadosCliente = "Nome: " + cliente.getNome() + "\nE-mail: " + cliente.getEmail() + "\nData de nascimento: " +
                    (cliente.getDataNascimento() != null ? cliente.getDataNascimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-");

            int confirmacao = JOptionPane.showConfirmDialog(frameConsulta,
                    "Deseja realmente excluir este cliente?\n\n" + dadosCliente,
                    "Confirmar exclusão",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirmacao == JOptionPane.YES_OPTION) {
                try {
                    clienteService.excluir(id);
                    estadoPaginacao.paginaAtual = 0;
                    carregarPagina.accept(null);
                    JOptionPane.showMessageDialog(frameConsulta, "Cliente excluído com sucesso.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frameConsulta, "Erro ao excluir cliente: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

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

        var painelTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        painelTitulo.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        painelTitulo.setBackground(new java.awt.Color(240, 240, 240));
        var lblTitulo = new JLabel("Gestão de Clientes");
        lblTitulo.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
        painelTitulo.add(lblTitulo);

        var painelFiltro = new JPanel(new GridLayout(2, 4, 8, 8));
        painelFiltro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Filtro"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        var campoBuscaNome = new JTextField(15);
        painelFiltro.add(new JLabel("Nome:"));
        painelFiltro.add(campoBuscaNome);

        var campoBuscaEmail = new JTextField(15);
        painelFiltro.add(new JLabel("E-mail:"));
        painelFiltro.add(campoBuscaEmail);

        var campoBuscaData = new DateInput();
        painelFiltro.add(new JLabel("Data de Nascimento:"));
        painelFiltro.add(campoBuscaData);

        painelFiltro.add(new JLabel(""));
        painelFiltro.add(new JLabel(""));

        var painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));

        var btnPesquisar = new JButton("Pesquisar");
        btnPesquisar.addActionListener(_ -> {
            var temNome = campoBuscaNome.getText() != null && !campoBuscaNome.getText().trim().isEmpty();
            var temEmail = campoBuscaEmail.getText() != null && !campoBuscaEmail.getText().trim().isEmpty();
            var dataFiltro = campoBuscaData.getDate();
            var temData = dataFiltro != null;

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
                estadoPaginacao.filtroData = dataFiltro;
                estadoPaginacao.filtroNome = "";
                estadoPaginacao.filtroEmail = "";
                estadoPaginacao.tipoFiltro = 3;
                carregarPagina.accept(null);
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
            campoBuscaData.limpar();
            estadoPaginacao.filtroNome = "";
            estadoPaginacao.filtroEmail = "";
            estadoPaginacao.filtroData = null;
            estadoPaginacao.tipoFiltro = 0;
            estadoPaginacao.paginaAtual = 0;
            carregarPagina.accept(null);
        });

        painelBotoes.add(btnPesquisar);
        painelBotoes.add(btnLimpar);

        var painelBuscaSuperior = new JPanel(new BorderLayout(8, 8));
        painelBuscaSuperior.add(painelFiltro, BorderLayout.NORTH);
        painelBuscaSuperior.add(painelBotoes, BorderLayout.CENTER);

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
            var pagina = clienteService.listarPaginado(0, estadoPaginacao.tamanho);
            if (estadoPaginacao.tipoFiltro == 1 && !estadoPaginacao.filtroNome.isEmpty()) {
                pagina = clienteService.buscarPorNomePaginado(estadoPaginacao.filtroNome, 0, estadoPaginacao.tamanho);
            } else if (estadoPaginacao.tipoFiltro == 2 && !estadoPaginacao.filtroEmail.isEmpty()) {
                pagina = clienteService.buscarPorEmailPaginado(estadoPaginacao.filtroEmail, 0, estadoPaginacao.tamanho);
            } else if (estadoPaginacao.tipoFiltro == 3 && estadoPaginacao.filtroData != null) {
                pagina = clienteService.buscarPorDataNascimentoPaginado(estadoPaginacao.filtroData, 0, estadoPaginacao.tamanho);
            }
            estadoPaginacao.paginaAtual = Math.max(0, pagina.getTotalPages() - 1);
            carregarPagina.accept(null);
        });

        painelPaginacao.add(btnPrimeira);
        painelPaginacao.add(btnAnterior);
        painelPaginacao.add(infoPaginacao);
        painelPaginacao.add(btnProxima);
        painelPaginacao.add(btnUltima);

        var painelInferior = new JPanel(new BorderLayout(8, 8));
        painelInferior.add(painelEsquerdo, BorderLayout.WEST);
        painelInferior.add(painelPaginacao, BorderLayout.EAST);

        var painelPrincipalSuperior = new JPanel(new BorderLayout(8, 8));
        painelPrincipalSuperior.add(painelTitulo, BorderLayout.NORTH);
        painelPrincipalSuperior.add(new JSeparator(), BorderLayout.CENTER);
        painelPrincipalSuperior.add(painelBuscaSuperior, BorderLayout.SOUTH);

        frameConsulta.add(painelPrincipalSuperior, BorderLayout.NORTH);
        frameConsulta.add(scrollTabela, BorderLayout.CENTER);
        frameConsulta.add(painelInferior, BorderLayout.SOUTH);

        carregarPagina.accept(null);
        frameConsulta.revalidate();
        frameConsulta.repaint();
    }

    private void abrirDialogCadastro(JFrame parent, java.util.function.Consumer<Object> aposXadastro, Object estadoPaginacao) {
        var dialog = new JDialog(parent, "Cadastrar novo cliente", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout(8, 8));

        var nome = new JTextField();
        var email = new JTextField();
        var telefone = new JTextField();
        var dataNascimento = new DateInput();
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
                LocalDate data = dataNascimento.getDate();
                if (dataNascimento.getText() != null && !dataNascimento.getText().trim().isEmpty() && data == null) {
                    throw new IllegalArgumentException("Data de nascimento inválida. Use o formato dd/MM/yyyy");
                }
                clienteService.cadastrar(nome.getText(), email.getText(), telefone.getText(), data);
                status.setText("✓ Cliente cadastrado com sucesso!");
                nome.setText("");
                email.setText("");
                telefone.setText("");
                dataNascimento.limpar();

                var pag = (Object) estadoPaginacao;
                java.lang.reflect.Field field = pag.getClass().getDeclaredField("paginaAtual");
                field.setAccessible(true);
                field.setInt(pag, 0);
                aposXadastro.accept(null);

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

    private void abrirDialogCliente(JFrame parent, Cliente cliente, java.util.function.Consumer<Object> aposXadastro, boolean editar) {
        var dialog = new JDialog(parent, (editar ? "Editar" : "Visualizar") + " cliente", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout(8, 8));

        var nome = new JTextField();
        var email = new JTextField();
        var telefone = new JTextField();
        var dataNascimento = new DateInput();
        var status = new JLabel(" ");

        nome.setText(cliente.getNome());
        email.setText(cliente.getEmail());
        telefone.setText(cliente.getTelefone());
        dataNascimento.setDate(cliente.getDataNascimento());

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

        var btnSalvar = new JButton(editar ? "Salvar alterações" : "Fechar");
        btnSalvar.addActionListener(_ -> {
            if (!editar) {
                dialog.dispose();
                return;
            }

            try {
                LocalDate data = dataNascimento.getDate();
                if (dataNascimento.getText() != null && !dataNascimento.getText().trim().isEmpty() && data == null) {
                    throw new IllegalArgumentException("Data de nascimento inválida. Use o formato dd/MM/yyyy");
                }
                clienteService.atualizar(cliente.getId(), nome.getText(), email.getText(), telefone.getText(), data);
                status.setText("✓ Cliente atualizado com sucesso!");
                aposXadastro.accept(null);
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
        botoes.add(btnSalvar);
        botoes.add(btnCancelar);

        var rodape = new JPanel(new BorderLayout(8, 8));
        rodape.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        rodape.add(status, BorderLayout.WEST);
        rodape.add(botoes, BorderLayout.EAST);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(rodape, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
