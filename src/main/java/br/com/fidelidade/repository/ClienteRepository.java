package br.com.fidelidade.repository;

import br.com.fidelidade.domain.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByEmailIgnoreCase(String email);
    Optional<Cliente> findByEmailIgnoreCase(String email);
    List<Cliente> findByNomeContainingIgnoreCase(String nome);
    List<Cliente> findByEmailContainingIgnoreCase(String email);
    List<Cliente> findByDataNascimento(LocalDate dataNascimento);
    Page<Cliente> findAllByOrderByNomeAsc(Pageable pageable);
    Page<Cliente> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome, Pageable pageable);
    Page<Cliente> findByEmailContainingIgnoreCaseOrderByNomeAsc(String email, Pageable pageable);
    Page<Cliente> findByDataNascimentoOrderByNomeAsc(LocalDate dataNascimento, Pageable pageable);
}
