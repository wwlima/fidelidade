package br.com.fidelidade.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import br.com.fidelidade.service.ProdutoService;

public class TelaProdutos {
    private final ProdutoService service;

    public TelaProdutos(ProdutoService service) {
        this.service = service;
    }

    public void abrirTelaConsulta(JFrame frameConsulta) {
        frameConsulta.setTitle("Fidelidade — Produtos");
        frameConsulta.getContentPane().removeAll();
        frameConsulta.setLayout(new BorderLayout(8, 8));

        // Estado da paginação
        var estadoPaginacao = new Object() {
            int paginaAtual = 0;
            int tamanho = 10;
            String filtroNome = "";
            int tipoFiltro = 0;
        };

        // Tabela de resultados
        var colunas = new String[] { "ID", "Nome" };
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
                }
                
                for (var produto : pagina.getContent()) {
                    modeloTabela.addRow(new Object[] { 
                        produto.getId(), 
                        produto.getNome()
                    });
                }
                
                infoPaginacao.setText("Página " + (estadoPaginacao.paginaAtual + 1) + " de " + (pagina.getTotalPages() == 0 ? 1 : pagina.getTotalPages()));
            }
        };

        // Painel esquerdo com botões
        var painelEsquerdo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        painelEsquerdo.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        var btnCadastrar = new JButton("+ Cadastrar novo produto");
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
        var lblTitulo = new JLabel("Gestão de Produtos");
        lblTitulo.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
        painelTitulo.add(lblTitulo);

        // Painel de filtro/pesquisa
        var painelFiltro = new JPanel(new GridLayout(1, 2, 8, 8));
        painelFiltro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Filtro"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        var campoBuscaNome = new JTextField(15);
        painelFiltro.add(new JLabel("Nome:"));
        painelFiltro.add(campoBuscaNome);

        // Painel de botões de ação
        var painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));
        
        var btnPesquisar = new JButton("Pesquisar");
        btnPesquisar.addActionListener(_ -> {
            var temNome = campoBuscaNome.getText() != null && !campoBuscaNome.getText().trim().isEmpty();
            
            estadoPaginacao.paginaAtual = 0;
            
            if (temNome) {
                estadoPaginacao.filtroNome = campoBuscaNome.getText().trim();
                estadoPaginacao.tipoFiltro = 1;
                carregarPagina.accept(null);
            } else {
                estadoPaginacao.filtroNome = "";
                estadoPaginacao.tipoFiltro = 0;
                carregarPagina.accept(null);
            }
        });
        
        var btnLimpar = new JButton("Limpar");
        btnLimpar.addActionListener(_ -> {
            campoBuscaNome.setText("");
            estadoPaginacao.filtroNome = "";
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
            }
            estadoPaginacao.paginaAtual = Math.max(0, pagina.getTotalPages() - 1);
            carregarPagina.accept(null);
        });
        
        painelPaginacao.add(btnPrimeira);
        painelPaginacao.add(btnAnterior);
        painelPaginacao.add(infoPaginacao);
        painelPaginacao.add(btnProxima);
        painelPaginacao.add(btnUltima);

        // Painel de ações (Editar e Excluir)
        var painelAcoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        painelAcoes.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        
        var btnEditar = new JButton("✎ Editar");
        btnEditar.addActionListener(_ -> {
            int linha = tabela.getSelectedRow();
            if (linha < 0) {
                JOptionPane.showMessageDialog(frameConsulta, "Selecione um produto para editar");
                return;
            }
            var id = (Long) modeloTabela.getValueAt(linha, 0);
            var nome = (String) modeloTabela.getValueAt(linha, 1);
            abrirDialogEdicao(frameConsulta, id, nome, carregarPagina);
        });
        
        var btnExcluir = new JButton("✕ Excluir");
        btnExcluir.addActionListener(_ -> {
            int linha = tabela.getSelectedRow();
            if (linha < 0) {
                JOptionPane.showMessageDialog(frameConsulta, "Selecione um produto para excluir");
                return;
            }
            var id = (Long) modeloTabela.getValueAt(linha, 0);
            var nome = (String) modeloTabela.getValueAt(linha, 1);
            int confirmacao = JOptionPane.showConfirmDialog(frameConsulta, 
                "Tem certeza que deseja excluir o produto '" + nome + "'?", 
                "Confirmação", 
                JOptionPane.YES_NO_OPTION);
            if (confirmacao == JOptionPane.YES_OPTION) {
                try {
                    service.excluir(id);
                    carregarPagina.accept(null);
                    JOptionPane.showMessageDialog(frameConsulta, "Produto excluído com sucesso!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frameConsulta, "Erro ao excluir: " + ex.getMessage());
                }
            }
        });
        
        painelAcoes.add(btnEditar);
        painelAcoes.add(btnExcluir);

        // Painel inferior combinado: ações + paginação
        var painelInferior = new JPanel(new BorderLayout(8, 8));
        painelInferior.add(painelEsquerdo, BorderLayout.WEST);
        painelInferior.add(painelAcoes, BorderLayout.CENTER);
        painelInferior.add(painelPaginacao, BorderLayout.EAST);

        // Painel principal superior combinado: título + separador + busca
        var painelPrincipalSuperior = new JPanel(new BorderLayout(8, 8));
        painelPrincipalSuperior.add(painelTitulo, BorderLayout.NORTH);
        painelPrincipalSuperior.add(new JSeparator(), BorderLayout.CENTER);
        painelPrincipalSuperior.add(painelBuscaSuperior, BorderLayout.SOUTH);

        frameConsulta.add(painelPrincipalSuperior, BorderLayout.NORTH);
        frameConsulta.add(scrollTabela, BorderLayout.CENTER);
        frameConsulta.add(painelInferior, BorderLayout.SOUTH);
        
        // Carregar os primeiros 10 registros ao abrir
        carregarPagina.accept(null);
        
        frameConsulta.revalidate();
        frameConsulta.repaint();
    }

    private void abrirDialogCadastro(JFrame parent, java.util.function.Consumer<Object> aposXadastro, Object estadoPaginacao) {
        var dialog = new JDialog(parent, "Cadastrar novo produto", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout(8, 8));

        var nome = new JTextField();
        var status = new JLabel(" ");

        var form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        form.add(new JLabel("Nome do produto *"));
        form.add(nome);

        var btnCadastrar = new JButton("Cadastrar");
        btnCadastrar.addActionListener(_ -> {
            try {
                service.cadastrar(nome.getText());
                status.setText("✓ Produto cadastrado com sucesso!");
                nome.setText("");
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

    private void abrirDialogEdicao(JFrame parent, Long id, String nomeAtual, java.util.function.Consumer<Object> aoAtualizar) {
        var dialog = new JDialog(parent, "Editar produto", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout(8, 8));

        var nome = new JTextField(nomeAtual);
        var status = new JLabel(" ");

        var form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        form.add(new JLabel("Nome do produto *"));
        form.add(nome);

        var btnAtualizar = new JButton("Atualizar");
        btnAtualizar.addActionListener(_ -> {
            try {
                service.atualizar(id, nome.getText());
                status.setText("✓ Produto atualizado com sucesso!");
                // Atualizar lista após edição
                aoAtualizar.accept(null);
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
        botoes.add(btnAtualizar);
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
