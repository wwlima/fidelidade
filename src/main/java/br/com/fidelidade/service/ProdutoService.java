package br.com.fidelidade.service;

import br.com.fidelidade.domain.Produto;
import br.com.fidelidade.repository.ProdutoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {
    private final ProdutoRepository repository;
    public ProdutoService(ProdutoRepository repository) { this.repository = repository; }
    
    @Transactional
    public Produto cadastrar(String nome) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome é obrigatório");
        if (repository.existsByNomeIgnoreCase(nome)) throw new IllegalArgumentException("Já existe produto com este nome");
        return repository.save(new Produto(nome.trim()));
    }
    
    @Transactional
    public Produto atualizar(Long id, String nome) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome é obrigatório");
        var produto = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
        if (!produto.getNome().equalsIgnoreCase(nome) && repository.existsByNomeIgnoreCase(nome)) {
            throw new IllegalArgumentException("Já existe outro produto com este nome");
        }
        produto.setNome(nome.trim());
        return repository.save(produto);
    }
    
    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) throw new IllegalArgumentException("Produto não encontrado");
        repository.deleteById(id);
    }
    
    public Optional<Produto> buscarPorId(Long id) {
        return repository.findById(id);
    }
    
    public List<Produto> buscarPorNome(String nome) {
        if (nome == null || nome.isBlank()) return List.of();
        return repository.findByNomeContainingIgnoreCase(nome.trim());
    }
    
    public Page<Produto> listarPaginado(int pagina, int tamanho) {
        return repository.findAllByOrderByNomeAsc(PageRequest.of(pagina, tamanho));
    }
    
    public Page<Produto> buscarPorNomePaginado(String nome, int pagina, int tamanho) {
        if (nome == null || nome.isBlank()) return Page.empty();
        return repository.findByNomeContainingIgnoreCaseOrderByNomeAsc(nome.trim(), PageRequest.of(pagina, tamanho));
    }
}
