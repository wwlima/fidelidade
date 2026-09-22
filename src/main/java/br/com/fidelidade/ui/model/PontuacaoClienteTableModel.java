package br.com.fidelidade.ui.model;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import br.com.fidelidade.dto.PontuacaoClienteDTO;

public class PontuacaoClienteTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private static final String[] COLUNAS = { "Cliente", "Produto", "Campanha", "Quantidade", "Data Cadastro" };
	private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private final List<PontuacaoClienteDTO> registros = new ArrayList<>();

	public void adicionar(PontuacaoClienteDTO dto) {
		registros.add(dto);
		int linha = registros.size() - 1;
		fireTableRowsInserted(linha, linha);
	}

	public void recarregar(List<PontuacaoClienteDTO> novosRegistros) {
		registros.clear();
		registros.addAll(novosRegistros);
		fireTableDataChanged();
	}

	@Override
	public int getRowCount() {
		return registros.size();
	}

	@Override
	public int getColumnCount() {
		return COLUNAS.length;
	}

	@Override
	public String getColumnName(int coluna) {
		return COLUNAS[coluna];
	}

	@Override
	public Object getValueAt(int linha, int coluna) {
		PontuacaoClienteDTO dto = registros.get(linha);
		return switch (coluna) {
		case 0 -> dto.clienteNome();
		case 1 -> dto.produtoNome();
		case 2 -> dto.campanhaNome();
		case 3 -> dto.quantidade();
		case 4 -> dto.dataCadastro().format(FORMATO);
		default -> null;
		};
	}
}
