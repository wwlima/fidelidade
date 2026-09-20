package br.com.fidelidade.service;

import br.com.fidelidade.domain.Campanha;
import br.com.fidelidade.repository.CampanhaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CampanhaService {
    private final CampanhaRepository repository;

    public CampanhaService(CampanhaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Campanha cadastrar(String nome, LocalDate dataInicioVigencia, LocalDate dataFimVigencia, boolean ativo) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome é obrigatório");
        if (dataInicioVigencia == null) throw new IllegalArgumentException("Data de início da vigência é obrigatória");
        if (dataFimVigencia == null) throw new IllegalArgumentException("Data final da vigência é obrigatória");
        if (dataFimVigencia.isBefore(dataInicioVigencia)) throw new IllegalArgumentException("A data final não pode ser anterior à data inicial");
        if (repository.existsByNomeIgnoreCase(nome)) throw new IllegalArgumentException("Já existe campanha com este nome");

        var campanha = new Campanha(nome.trim(), dataInicioVigencia, dataFimVigencia, ativo);
        campanha.setDataAtualizacao(LocalDateTime.now());
        return repository.save(campanha);
    }

    @Transactional
    public Campanha atualizar(Long id, String nome, LocalDate dataInicioVigencia, LocalDate dataFimVigencia, boolean ativo) {
        if (id == null) throw new IllegalArgumentException("Identificador da campanha é obrigatório");
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome é obrigatório");
        if (dataInicioVigencia == null) throw new IllegalArgumentException("Data de início da vigência é obrigatória");
        if (dataFimVigencia == null) throw new IllegalArgumentException("Data final da vigência é obrigatória");
        if (dataFimVigencia.isBefore(dataInicioVigencia)) throw new IllegalArgumentException("A data final não pode ser anterior à data inicial");

        var campanha = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Campanha não encontrada"));
        var mesmaNome = repository.findById(id).orElse(null);
        if (mesmaNome != null && !campanha.getNome().equalsIgnoreCase(nome.trim()) && repository.existsByNomeIgnoreCase(nome)) {
            throw new IllegalArgumentException("Já existe outra campanha com este nome");
        }

        campanha.setNome(nome.trim());
        campanha.setDataInicioVigencia(dataInicioVigencia);
        campanha.setDataFimVigencia(dataFimVigencia);
        campanha.setAtivo(ativo);
        campanha.setDataAtualizacao(LocalDateTime.now());
        return repository.save(campanha);
    }

    @Transactional
    public void excluir(Long id) {
        if (id == null) throw new IllegalArgumentException("Identificador da campanha é obrigatório");
        if (!repository.existsById(id)) throw new IllegalArgumentException("Campanha não encontrada");
        repository.deleteById(id);
    }

    public Optional<Campanha> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Page<Campanha> listarPaginado(int pagina, int tamanho) {
        return repository.findAllByOrderByNomeAsc(PageRequest.of(pagina, tamanho));
    }

    public Page<Campanha> buscarPorNomePaginado(String nome, int pagina, int tamanho) {
        if (nome == null || nome.isBlank()) return Page.empty();
        return repository.findByNomeContainingIgnoreCaseOrderByNomeAsc(nome.trim(), PageRequest.of(pagina, tamanho));
    }

    public Page<Campanha> buscarAtivasPaginado(int pagina, int tamanho) {
        return repository.findByAtivoTrueOrderByNomeAsc(PageRequest.of(pagina, tamanho));
    }

    public Page<Campanha> buscarPorNomeAtivasPaginado(String nome, int pagina, int tamanho) {
        if (nome == null || nome.isBlank()) return Page.empty();
        return repository.findByNomeContainingIgnoreCaseAndAtivoTrueOrderByNomeAsc(nome.trim(), PageRequest.of(pagina, tamanho));
    }
}
