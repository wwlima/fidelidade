package br.com.fidelidade.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class DateInput extends JPanel {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("pt", "BR"));

    private final JTextField campoData = new JTextField();
    private final JButton botaoCalendario = new JButton("📅");
    private final JPopupMenu popupCalendario = new JPopupMenu();
    private final JPanel painelCalendario = new JPanel(new BorderLayout(8, 8));
    private final List<JButton> botoesMes = new ArrayList<>();

    private LocalDate mesExibido = LocalDate.now();
    private LocalDate dataSelecionada;
    private boolean atualizandoCampo;

    public DateInput() {
        super(new BorderLayout(6, 0));
        setOpaque(false);
        setPreferredSize(new Dimension(170, 32));

        ((AbstractDocument) campoData.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) {
                    return;
                }
                replace(fb, offset, 0, string, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                String textoAtual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String entrada = (text == null ? "" : text);
                String combinado = textoAtual.substring(0, offset) + entrada + textoAtual.substring(offset + length);
                String formatado = formatarTexto(combinado);
                super.replace(fb, 0, fb.getDocument().getLength(), formatado, attrs);
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                String textoAtual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String antes = textoAtual.substring(0, offset);
                String depois = textoAtual.substring(offset + length);
                super.replace(fb, 0, fb.getDocument().getLength(), formatarTexto(antes + depois), null);
            }
        });

        campoData.setColumns(10);
        campoData.setHorizontalAlignment(SwingConstants.CENTER);
        campoData.setToolTipText("dd/MM/yyyy");
        campoData.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { formatarCampo(); }
            @Override public void removeUpdate(DocumentEvent e) { formatarCampo(); }
            @Override public void changedUpdate(DocumentEvent e) { formatarCampo(); }
        });
        campoData.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                normalizarCampo();
            }
        });

        botaoCalendario.setFocusable(false);
        botaoCalendario.addActionListener(_ -> {
            if (popupCalendario.isVisible()) {
                popupCalendario.setVisible(false);
                return;
            }
            montarCalendario();
            popupCalendario.show(this, 0, getHeight());
        });

        add(campoData, BorderLayout.CENTER);
        add(botaoCalendario, BorderLayout.EAST);

        popupCalendario.setBorder(BorderFactory.createEmptyBorder());
        popupCalendario.add(painelCalendario);
    }

    private String formatarTexto(String texto) {
        String numeros = texto == null ? "" : texto.replaceAll("\\D", "");
        if (numeros.length() > 8) {
            numeros = numeros.substring(0, 8);
        }

        StringBuilder formatado = new StringBuilder();
        for (int i = 0; i < numeros.length(); i++) {
            if (i == 2 || i == 4) {
                formatado.append('/');
            }
            formatado.append(numeros.charAt(i));
        }
        return formatado.toString();
    }

    private void formatarCampo() {
        if (atualizandoCampo) {
            return;
        }

        String valor = campoData.getText() == null ? "" : campoData.getText();
        String formatado = formatarTexto(valor);
        if (!formatado.equals(valor)) {
            atualizandoCampo = true;
            campoData.setText(formatado);
            atualizandoCampo = false;
        }

        if (formatado.length() == 10) {
            try {
                dataSelecionada = LocalDate.parse(formatado, FORMATO);
            } catch (DateTimeParseException ex) {
                dataSelecionada = null;
            }
        } else {
            dataSelecionada = null;
        }
    }

    private void normalizarCampo() {
        if (atualizandoCampo) {
            return;
        }

        String texto = campoData.getText() == null ? "" : campoData.getText().trim();
        if (texto.isEmpty()) {
            dataSelecionada = null;
            return;
        }

        try {
            dataSelecionada = LocalDate.parse(texto, FORMATO);
            campoData.setText(dataSelecionada.format(FORMATO));
            mesExibido = dataSelecionada;
        } catch (DateTimeParseException ex) {
            dataSelecionada = null;
        }
    }

    public String getText() {
        return campoData.getText();
    }

    public LocalDate getDate() {
        String texto = campoData.getText();
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim(), FORMATO);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    public void setDate(LocalDate data) {
        dataSelecionada = data;
        if (data == null) {
            campoData.setText("");
            return;
        }
        atualizandoCampo = true;
        campoData.setText(data.format(FORMATO));
        atualizandoCampo = false;
        mesExibido = data;
    }

    public void limpar() {
        dataSelecionada = null;
        atualizandoCampo = true;
        campoData.setText("");
        atualizandoCampo = false;
        mesExibido = LocalDate.now();
    }

    private void montarCalendario() {
        painelCalendario.removeAll();
        painelCalendario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JPanel cabecalho = new JPanel(new BorderLayout(8, 0));
        JButton btnMesAnterior = new JButton("‹");
        JButton btnMesProximo = new JButton("›");
        JLabel labelMes = new JLabel(mesExibido.format(FORMATO_MES), SwingConstants.CENTER);

        btnMesAnterior.addActionListener(_ -> {
            mesExibido = mesExibido.minusMonths(1);
            atualizarCalendario(labelMes);
        });
        btnMesProximo.addActionListener(_ -> {
            mesExibido = mesExibido.plusMonths(1);
            atualizarCalendario(labelMes);
        });

        cabecalho.add(btnMesAnterior, BorderLayout.WEST);
        cabecalho.add(labelMes, BorderLayout.CENTER);
        cabecalho.add(btnMesProximo, BorderLayout.EAST);

        JPanel gradeDias = new JPanel(new GridLayout(7, 7, 4, 4));
        String[] nomesDias = {"D", "S", "T", "Q", "Q", "S", "S"};
        for (String nome : nomesDias) {
            JLabel label = new JLabel(nome, SwingConstants.CENTER);
            label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD, 11f));
            gradeDias.add(label);
        }

        botoesMes.clear();
        for (int i = 0; i < 42; i++) {
            JButton botaoDia = new JButton();
            botaoDia.setFocusable(false);
            botaoDia.setMargin(new java.awt.Insets(2, 4, 2, 4));
            botaoDia.setBackground(Color.WHITE);
            botoesMes.add(botaoDia);
            gradeDias.add(botaoDia);
        }

        painelCalendario.add(cabecalho, BorderLayout.NORTH);
        painelCalendario.add(gradeDias, BorderLayout.CENTER);
        atualizarCalendario(labelMes);

        painelCalendario.revalidate();
        painelCalendario.repaint();
    }

    private void atualizarCalendario(JLabel labelMes) {
        labelMes.setText(mesExibido.format(FORMATO_MES));

        LocalDate primeiroDia = mesExibido.withDayOfMonth(1);
        int deslocamento = primeiroDia.getDayOfWeek().getValue() % 7;
        LocalDate inicio = primeiroDia.minusDays(deslocamento);

        for (int i = 0; i < botoesMes.size(); i++) {
            LocalDate dataDoBotao = inicio.plusDays(i);
            JButton botao = botoesMes.get(i);
            botao.setText(String.valueOf(dataDoBotao.getDayOfMonth()));
            botao.putClientProperty("data", dataDoBotao);
            botao.setEnabled(true);
            botao.setOpaque(true);
            botao.setBorder(BorderFactory.createEmptyBorder());
            botao.setBackground(dataSelecionada != null && dataDoBotao.equals(dataSelecionada) ? new Color(104, 160, 255) : Color.WHITE);
            botao.setForeground(dataSelecionada != null && dataDoBotao.equals(dataSelecionada) ? Color.WHITE :
                    (dataDoBotao.getMonthValue() == mesExibido.getMonthValue() ? Color.DARK_GRAY : Color.LIGHT_GRAY));

            botao.addActionListener(_ -> {
                LocalDate dataAtual = (LocalDate) botao.getClientProperty("data");
                if (dataAtual != null) {
                    selecionarData(dataAtual);
                }
            });
        }
    }

    private void selecionarData(LocalDate data) {
        dataSelecionada = data;
        mesExibido = data;
        atualizandoCampo = true;
        campoData.setText(data.format(FORMATO));
        atualizandoCampo = false;
        popupCalendario.setVisible(false);
    }
}