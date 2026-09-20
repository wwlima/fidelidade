package br.com.fidelidade.repository;

import br.com.fidelidade.domain.Campanha;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampanhaRepository extends JpaRepository<Campanha, Long> {
    boolean existsByNomeIgnoreCase(String nome);
    Page<Campanha> findAllByOrderByNomeAsc(Pageable pageable);
    Page<Campanha> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome, Pageable pageable);
    Page<Campanha> findByAtivoTrueOrderByNomeAsc(Pageable pageable);
    Page<Campanha> findByNomeContainingIgnoreCaseAndAtivoTrueOrderByNomeAsc(String nome, Pageable pageable);
}
