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
import javax.swing.JCheckBox;
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

import br.com.fidelidade.domain.Campanha;
import br.com.fidelidade.service.CampanhaService;

public class TelaCampanhas {
    private final CampanhaService campanhaService;

    public TelaCampanhas(CampanhaService campanhaService) {
        this.campanhaService = campanhaService;
    }

    public void abrirTelaConsulta(JFrame frameConsulta) {
        frameConsulta.setTitle("Fidelidade — Campanhas");
        frameConsulta.getContentPane().removeAll();
        frameConsulta.setLayout(new BorderLayout(8, 8));

        var estadoPaginacao = new Object() {
            int paginaAtual = 0;
            int tamanho = 10;
            String filtroNome = "";
            boolean filtroAtivo = false;
            int tipoFiltro = 0;
        };

        var colunas = new String[] { "ID", "Nome", "Data Início", "Data Fim", "Ativa" };
        var modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        var tabela = new JTable(modeloTabela);
        var menuContexto = new JPopupMenu();
        var itemEditar = new JMenuItem("Editar campanha");
        var itemExcluir = new JMenuItem("Excluir campanha");
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
                var pagina = campanhaService.listarPaginado(estadoPaginacao.paginaAtual, estadoPaginacao.tamanho);

                if (estadoPaginacao.tipoFiltro == 1 && !estadoPaginacao.filtroNome.isEmpty()) {
                    pagina = campanhaService.buscarPorNomePaginado(estadoPaginacao.filtroNome, estadoPaginacao.paginaAtual, estadoPaginacao.tamanho);
                } else if (estadoPaginacao.tipoFiltro == 2 && estadoPaginacao.filtroAtivo) {
                    pagina = campanhaService.buscarAtivasPaginado(estadoPaginacao.paginaAtual, estadoPaginacao.tamanho);
                } else if (estadoPaginacao.tipoFiltro == 3 && !estadoPaginacao.filtroNome.isEmpty() && estadoPaginacao.filtroAtivo) {
                    pagina = campanhaService.buscarPorNomeAtivasPaginado(estadoPaginacao.filtroNome, estadoPaginacao.paginaAtual, estadoPaginacao.tamanho);
                }

                for (var campanha : pagina.getContent()) {
                    modeloTabela.addRow(new Object[] {
                        campanha.getId(),
                        campanha.getNome(),
                        campanha.getDataInicioVigencia() != null ? campanha.getDataInicioVigencia().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "",
                        campanha.getDataFimVigencia() != null ? campanha.getDataFimVigencia().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "",
                        campanha.isAtivo() ? "Sim" : "Não"
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
            Campanha campanha = campanhaService.buscarPorId(id).orElse(null);
            if (campanha != null) {
                abrirDialogCampanha(frameConsulta, campanha, carregarPagina, true);
            }
        });

        itemExcluir.addActionListener(_ -> {
            int linha = tabela.getSelectedRow();
            if (linha < 0) {
                return;
            }
            Long id = (Long) modeloTabela.getValueAt(linha, 0);
            Campanha campanha = campanhaService.buscarPorId(id).orElse(null);
            if (campanha == null) {
                return;
            }

            String dadosCampanha = "Nome: " + campanha.getNome() + "\nInício: " +
                    (campanha.getDataInicioVigencia() != null ? campanha.getDataInicioVigencia().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-") +
                    "\nFim: " + (campanha.getDataFimVigencia() != null ? campanha.getDataFimVigencia().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-");

            int confirmacao = JOptionPane.showConfirmDialog(frameConsulta,
                    "Deseja realmente excluir esta campanha?\n\n" + dadosCampanha,
                    "Confirmar exclusão",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirmacao == JOptionPane.YES_OPTION) {
                try {
                    campanhaService.excluir(id);
                    estadoPaginacao.paginaAtual = 0;
                    carregarPagina.accept(null);
                    JOptionPane.showMessageDialog(frameConsulta, "Campanha excluída com sucesso.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frameConsulta, "Erro ao excluir campanha: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        var painelEsquerdo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        painelEsquerdo.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        var btnCadastrar = new JButton("+ Cadastrar nova campanha");
        btnCadastrar.addActionListener(_ -> abrirDialogCampanha(frameConsulta, null, carregarPagina, false));
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
        var lblTitulo = new JLabel("Gestão de Campanhas");
        lblTitulo.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
        painelTitulo.add(lblTitulo);

        var painelFiltro = new JPanel(new GridLayout(2, 3, 8, 8));
        painelFiltro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Filtro"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        var campoBuscaNome = new JTextField(15);
        painelFiltro.add(new JLabel("Nome:"));
        painelFiltro.add(campoBuscaNome);

        var chkAtivas = new JCheckBox("Somente ativas");
        painelFiltro.add(chkAtivas);

        painelFiltro.add(new JLabel(""));
        painelFiltro.add(new JLabel(""));
        painelFiltro.add(new JLabel(""));

        var painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));

        var btnPesquisar = new JButton("Pesquisar");
        btnPesquisar.addActionListener(_ -> {
            var temNome = campoBuscaNome.getText() != null && !campoBuscaNome.getText().trim().isEmpty();
            var somenteAtivas = chkAtivas.isSelected();
            estadoPaginacao.paginaAtual = 0;

            if (temNome && somenteAtivas) {
                estadoPaginacao.filtroNome = campoBuscaNome.getText().trim();
                estadoPaginacao.filtroAtivo = true;
                estadoPaginacao.tipoFiltro = 3;
                carregarPagina.accept(null);
            } else if (somenteAtivas) {
                estadoPaginacao.filtroNome = "";
                estadoPaginacao.filtroAtivo = true;
                estadoPaginacao.tipoFiltro = 2;
                carregarPagina.accept(null);
            } else if (temNome) {
                estadoPaginacao.filtroNome = campoBuscaNome.getText().trim();
                estadoPaginacao.filtroAtivo = false;
                estadoPaginacao.tipoFiltro = 1;
                carregarPagina.accept(null);
            } else {
                estadoPaginacao.filtroNome = "";
                estadoPaginacao.filtroAtivo = false;
                estadoPaginacao.tipoFiltro = 0;
                carregarPagina.accept(null);
            }
        });

        var btnLimpar = new JButton("Limpar");
        btnLimpar.addActionListener(_ -> {
            campoBuscaNome.setText("");
            chkAtivas.setSelected(false);
            estadoPaginacao.filtroNome = "";
            estadoPaginacao.filtroAtivo = false;
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
            var pagina = campanhaService.listarPaginado(0, estadoPaginacao.tamanho);
            if (estadoPaginacao.tipoFiltro == 1 && !estadoPaginacao.filtroNome.isEmpty()) {
                pagina = campanhaService.buscarPorNomePaginado(estadoPaginacao.filtroNome, 0, estadoPaginacao.tamanho);
            } else if (estadoPaginacao.tipoFiltro == 2 && estadoPaginacao.filtroAtivo) {
                pagina = campanhaService.buscarAtivasPaginado(0, estadoPaginacao.tamanho);
            } else if (estadoPaginacao.tipoFiltro == 3 && !estadoPaginacao.filtroNome.isEmpty() && estadoPaginacao.filtroAtivo) {
                pagina = campanhaService.buscarPorNomeAtivasPaginado(estadoPaginacao.filtroNome, 0, estadoPaginacao.tamanho);
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

    private void abrirDialogCampanha(JFrame parent, Campanha campanha, java.util.function.Consumer<Object> aposCadastro, boolean editar) {
        var dialog = new JDialog(parent, editar ? "Editar campanha" : "Cadastrar nova campanha", true);
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout(8, 8));

        var nome = new JTextField();
        var dataInicio = new DateInput();
        var dataFim = new DateInput();
        var ativo = new JCheckBox("Ativa");
        var status = new JLabel(" ");

        if (campanha != null) {
            nome.setText(campanha.getNome());
            dataInicio.setDate(campanha.getDataInicioVigencia());
            dataFim.setDate(campanha.getDataFimVigencia());
            ativo.setSelected(campanha.isAtivo());
        }

        var form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        form.add(new JLabel("Nome *"));
        form.add(nome);
        form.add(new JLabel("Data início vigência *"));
        form.add(dataInicio);
        form.add(new JLabel("Data fim vigência *"));
        form.add(dataFim);
        form.add(new JLabel("Ativa"));
        form.add(ativo);

        var btnSalvar = new JButton(editar ? "Salvar alterações" : "Salvar");
        btnSalvar.addActionListener(_ -> {
            try {
                LocalDate inicio = dataInicio.getDate();
                LocalDate fim = dataFim.getDate();

                if (nome.getText() == null || nome.getText().isBlank()) {
                    throw new IllegalArgumentException("Nome é obrigatório");
                }
                if (inicio == null) {
                    throw new IllegalArgumentException("Data de início da vigência é obrigatória");
                }
                if (fim == null) {
                    throw new IllegalArgumentException("Data final da vigência é obrigatória");
                }
                if (fim.isBefore(inicio)) {
                    throw new IllegalArgumentException("A data final não pode ser anterior à data inicial");
                }

                if (editar) {
                    campanhaService.atualizar(campanha.getId(), nome.getText(), inicio, fim, ativo.isSelected());
                    status.setText("✓ Campanha atualizada com sucesso!");
                } else {
                    campanhaService.cadastrar(nome.getText(), inicio, fim, ativo.isSelected());
                    status.setText("✓ Campanha cadastrada com sucesso!");
                }

                nome.setText("");
                dataInicio.limpar();
                dataFim.limpar();
                ativo.setSelected(true);
                aposCadastro.accept(null);

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
