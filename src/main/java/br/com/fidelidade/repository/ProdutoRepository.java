package br.com.fidelidade.repository;

import br.com.fidelidade.domain.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    boolean existsByNomeIgnoreCase(String nome);
    List<Produto> findByNomeContainingIgnoreCase(String nome);
    Page<Produto> findAllByOrderByNomeAsc(Pageable pageable);
    Page<Produto> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome, Pageable pageable);
}
