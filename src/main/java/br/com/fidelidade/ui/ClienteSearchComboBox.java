package br.com.fidelidade.ui;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import br.com.fidelidade.domain.Cliente;
import br.com.fidelidade.service.ClienteService;

public class ClienteSearchComboBox extends JComboBox<Cliente> {

	private static final long serialVersionUID = 1L;
	private static final int MINIMO_CARACTERES = 3;
    private static final long DEBOUNCE_MS = 300;

    private final ClienteService clienteService;
    private final ScheduledExecutorService debounceExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "cliente-search-debounce");
        t.setDaemon(true);
        return t;
    });

    private ScheduledFuture<?> pesquisaAgendada;
    private boolean atualizandoProgramaticamente = false;
    private Cliente clienteSelecionado;

    public ClienteSearchComboBox(ClienteService clienteService) {
        this.clienteService = clienteService;
        setEditable(true);
        setRenderer(new ClienteRenderer());
        configurarListenerDeDigitacao();
        configurarListenerDeSelecao();
    }

    public Cliente getClienteSelecionado() {
        return clienteSelecionado;
    }

    public void limpar() {
        atualizandoProgramaticamente = true;
        clienteSelecionado = null;
        removeAllItems();
        setPopupVisible(false);
        JTextField editor = (JTextField) getEditor().getEditorComponent();
        editor.setText("");
        atualizandoProgramaticamente = false;
    }

    private void configurarListenerDeDigitacao() {
        JTextField editor = (JTextField) getEditor().getEditorComponent();
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onDigitar(editor); }
            @Override public void removeUpdate(DocumentEvent e) { onDigitar(editor); }
            @Override public void changedUpdate(DocumentEvent e) { onDigitar(editor); }
        });
    }

    private void onDigitar(JTextField editor) {
        if (atualizandoProgramaticamente) {
            return;
        }

        clienteSelecionado = null;
        String texto = editor.getText();

        if (pesquisaAgendada != null && !pesquisaAgendada.isDone()) {
            pesquisaAgendada.cancel(false);
        }

        // Só pesquisa a partir do 3º caractere
        if (texto.trim().length() < MINIMO_CARACTERES) {
            SwingUtilities.invokeLater(() -> {
                atualizandoProgramaticamente = true;
                removeAllItems();
                setPopupVisible(false);
                atualizandoProgramaticamente = false;
            });
            return;
        }

        pesquisaAgendada = debounceExecutor.schedule(
            () -> SwingUtilities.invokeLater(() -> executarPesquisa(texto, editor)),
            DEBOUNCE_MS, TimeUnit.MILLISECONDS
        );
    }

    private void executarPesquisa(String texto, JTextField editor) {
        List<Cliente> resultados = clienteService.buscarPorNome(texto);

        int caretAntes = editor.getCaretPosition();
        String textoAtual = editor.getText();

        atualizandoProgramaticamente = true;
        removeAllItems();
        for (Cliente cliente : resultados) {
            addItem(cliente);
        }
        atualizandoProgramaticamente = false;

        // Restaura o texto digitado e a posição do cursor
        editor.setText(textoAtual);
        editor.setCaretPosition(Math.min(caretAntes, textoAtual.length()));

        setPopupVisible(!resultados.isEmpty());
        editor.requestFocusInWindow();
    }

    private void configurarListenerDeSelecao() {
        addActionListener(e -> {
            if (atualizandoProgramaticamente) {
                return;
            }
            Object selecionado = getSelectedItem();
            if (selecionado instanceof Cliente cliente) {
                selecionarCliente(cliente);
            }
        });
    }

    private void selecionarCliente(Cliente cliente) {
        clienteSelecionado = cliente;

        atualizandoProgramaticamente = true;
        JTextField editor = (JTextField) getEditor().getEditorComponent();
        editor.setText(cliente.getNome());
        atualizandoProgramaticamente = false;

        // Fecha o popup (o "grid" de sugestões some)
        setPopupVisible(false);

        // Move o foco para o próximo campo do formulário
        SwingUtilities.invokeLater(() ->
            KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent(this)
        );
    }

    private static class ClienteRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Cliente cliente) {
                setText(cliente.getNome() + " - " + cliente.getEmail());
            }
            return this;
        }
    }
}