package br.com.fidelidade.dto;

import java.time.LocalDateTime;

public record PontuacaoClienteDTO(
        Long id,
        String clienteNome,
        String produtoNome,
        String campanhaNome,
        Integer quantidade,
        LocalDateTime dataCadastro,
        LocalDateTime dataAtualizacao
) {
}