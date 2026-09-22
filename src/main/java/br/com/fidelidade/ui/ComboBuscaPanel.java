package br.com.fidelidade.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import java.awt.*;

public class ComboBuscaPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final JComboBox<String> combo;
    private final JTextField campoTexto;
    private final DefaultTableModel modeloTabela;
    private final JTable tabela;
    private final JPopupMenu popup;

    public ComboBuscaPanel(String rotulo, String[] colunasTabela, Dimension tamanhoScroll) {
        super(new BorderLayout(4, 4));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        combo = new JComboBox<>();
        combo.setEditable(true);
        campoTexto = (JTextField) combo.getEditor().getEditorComponent();

        modeloTabela = criarModeloTabela(colunasTabela);
        tabela = new JTable(modeloTabela);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);

        var scroll = new JScrollPane(tabela);
        scroll.setPreferredSize(tamanhoScroll);

        popup = new JPopupMenu();
        popup.add(scroll);

        add(new JLabel(rotulo), BorderLayout.NORTH);
        add(combo, BorderLayout.CENTER);
    }

    public void habilitar(boolean habilitado) {
        combo.setEnabled(habilitado);
    }

    public void limpar() {
        combo.setSelectedItem("");
        campoTexto.setText("");
        modeloTabela.setRowCount(0);
        popup.setVisible(false);
    }

    public void selecionarTexto(String texto) {
        combo.setSelectedItem(texto);
        popup.setVisible(false);
    }

    public void focar() {
        combo.requestFocusInWindow();
    }

    public void configurarBusca(Runnable acaoBusca) {
        campoTexto.getDocument().addDocumentListener(new DocumentListener() {
            private void onChange() { SwingUtilities.invokeLater(acaoBusca); }
            @Override public void insertUpdate(DocumentEvent e) { onChange(); }
            @Override public void removeUpdate(DocumentEvent e) { onChange(); }
            @Override public void changedUpdate(DocumentEvent e) { onChange(); }
        });
    }

    public void configurarSelecao(java.util.function.Consumer<Integer> aoSelecionarLinha) {
        tabela.getSelectionModel().addListSelectionListener(_ -> {
            int linha = tabela.getSelectedRow();
            if (linha >= 0) {
                aoSelecionarLinha.accept(linha);
            }
        });
    }

    public String getTextoDigitado() {
        String texto = campoTexto.getText();
        return texto == null ? "" : texto.trim();
    }

    public DefaultTableModel getModelo() {
        return modeloTabela;
    }

    public void mostrarPopup() {
        if (modeloTabela.getRowCount() > 0) {
            popup.show(combo, 0, combo.getHeight());
        } else {
            popup.setVisible(false);
        }
    }

    public void esconderPopup() {
        popup.setVisible(false);
    }

    private static DefaultTableModel criarModeloTabela(String[] colunas) {
        return new DefaultTableModel(colunas, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }
}