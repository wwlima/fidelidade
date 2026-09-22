package br.com.fidelidade.service;

import br.com.fidelidade.domain.Campanha;
import br.com.fidelidade.domain.Cliente;
import br.com.fidelidade.domain.PontuacaoCliente;
import br.com.fidelidade.domain.Produto;
import br.com.fidelidade.dto.PontuacaoClienteDTO;
import br.com.fidelidade.repository.PontuacaoClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PontuacaoClienteService {
    private final PontuacaoClienteRepository repository;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final CampanhaService campanhaService;

    public PontuacaoClienteService(PontuacaoClienteRepository repository,
                                    ClienteService clienteService,
                                    ProdutoService produtoService,
                                    CampanhaService campanhaService) {
        this.repository = repository;
        this.clienteService = clienteService;
        this.produtoService = produtoService;
        this.campanhaService = campanhaService;
    }

    @Transactional
    public PontuacaoClienteDTO pontuar(Long clienteId, Long produtoId, Long campanhaId) {
        if (clienteId == null || produtoId == null || campanhaId == null) {
            throw new IllegalArgumentException("Cliente, produto e campanha são obrigatórios");
        }

        Cliente cliente = clienteService.buscarPorId(clienteId);
        Produto produto = produtoService.buscarPorId(produtoId).orElse(null);
        Campanha campanha = campanhaService.buscarPorId(campanhaId).orElse(null);

        if (cliente == null) throw new IllegalArgumentException("Cliente não encontrado");
        if (produto == null) throw new IllegalArgumentException("Produto não encontrado");
        if (campanha == null) throw new IllegalArgumentException("Campanha não encontrada");

        Optional<PontuacaoCliente> existente = repository.findByClienteIdAndProdutoIdAndCampanhaId(clienteId, produtoId, campanhaId);

        PontuacaoCliente pontuacao;
        if (existente.isPresent()) {
            pontuacao = existente.get();
            pontuacao.setQuantidade((pontuacao.getQuantidade() == null ? 0 : pontuacao.getQuantidade()) + 1);
            pontuacao.setDataAtualizacao(LocalDateTime.now());
        } else {
            pontuacao = new PontuacaoCliente(cliente, produto, campanha);
            pontuacao.setQuantidade(1);
            pontuacao.setDataCadastro(LocalDateTime.now());
            pontuacao.setDataAtualizacao(LocalDateTime.now());
        }

        pontuacao = repository.save(pontuacao);

        // Conversão para DTO ainda dentro da transação (sessão aberta)
        return toDTO(pontuacao, cliente, produto, campanha);
    }

    public List<PontuacaoClienteDTO> listarPorCliente(Long clienteId) {
        if (clienteId == null) return List.of();
        return repository.findByClienteIdComRelacionamentos(clienteId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public Page<PontuacaoClienteDTO> listarPorClientePaginado(Long clienteId, int pagina, int tamanho) {
        if (clienteId == null) return Page.empty();
        return repository.findByClienteIdComRelacionamentos(clienteId, PageRequest.of(pagina, tamanho))
                .map(this::toDTO);
    }

    private PontuacaoClienteDTO toDTO(PontuacaoCliente p, Cliente cliente, Produto produto, Campanha campanha) {
        return new PontuacaoClienteDTO(
                p.getId(),
                cliente.getNome(),
                produto.getNome(),
                campanha.getNome(),
                p.getQuantidade(),
                p.getDataCadastro(),
                p.getDataAtualizacao()
        );
    }

    private PontuacaoClienteDTO toDTO(PontuacaoCliente p) {
        return toDTO(p, p.getCliente(), p.getProduto(), p.getCampanha());
    }
    
    @Transactional
    public Optional<PontuacaoClienteDTO> buscarPorClienteProdutoCampanha(Long clienteId, Long produtoId, Long campanhaId) {
        if (clienteId == null || produtoId == null || campanhaId == null) {
            return Optional.empty();
        }
        return repository.findByClienteIdAndProdutoIdAndCampanhaId(clienteId, produtoId, campanhaId)
                .map(this::toDTO);
    }
}