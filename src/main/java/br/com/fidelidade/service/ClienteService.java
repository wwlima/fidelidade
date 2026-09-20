package br.com.fidelidade.service;

import br.com.fidelidade.domain.Cliente;
import br.com.fidelidade.repository.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class ClienteService {
    private final ClienteRepository repository;
    public ClienteService(ClienteRepository repository) { this.repository = repository; }
    @Transactional
    public Cliente cadastrar(String nome, String email, String telefone, LocalDate dataNascimento) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome é obrigatório");
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) throw new IllegalArgumentException("E-mail inválido");
        if (repository.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("Já existe cliente com este e-mail");
        return repository.save(new Cliente(nome.trim(), email.trim().toLowerCase(), telefone == null ? "" : telefone.trim(), dataNascimento));
    }
    
    public List<Cliente> buscarPorNome(String nome) {
        if (nome == null || nome.isBlank()) return List.of();
        return repository.findByNomeContainingIgnoreCase(nome.trim());
    }
    
    public List<Cliente> buscarPorEmail(String email) {
        if (email == null || email.isBlank()) return List.of();
        return repository.findByEmailContainingIgnoreCase(email.trim());
    }
    
    public List<Cliente> buscarPorDataNascimento(LocalDate data) {
        if (data == null) return List.of();
        return repository.findByDataNascimento(data);
    }
    
    public Page<Cliente> listarPaginado(int pagina, int tamanho) {
        return repository.findAllByOrderByNomeAsc(PageRequest.of(pagina, tamanho));
    }
    
    public Page<Cliente> buscarPorNomePaginado(String nome, int pagina, int tamanho) {
        if (nome == null || nome.isBlank()) return Page.empty();
        return repository.findByNomeContainingIgnoreCaseOrderByNomeAsc(nome.trim(), PageRequest.of(pagina, tamanho));
    }
    
    public Page<Cliente> buscarPorEmailPaginado(String email, int pagina, int tamanho) {
        if (email == null || email.isBlank()) return Page.empty();
        return repository.findByEmailContainingIgnoreCaseOrderByNomeAsc(email.trim(), PageRequest.of(pagina, tamanho));
    }
    
    public Page<Cliente> buscarPorDataNascimentoPaginado(LocalDate data, int pagina, int tamanho) {
        if (data == null) return Page.empty();
        return repository.findByDataNascimentoOrderByNomeAsc(data, PageRequest.of(pagina, tamanho));
    }
}
