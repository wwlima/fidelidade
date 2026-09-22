package br.com.fidelidade.repository;

import br.com.fidelidade.domain.PontuacaoCliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PontuacaoClienteRepository extends JpaRepository<PontuacaoCliente, Long> {

    Optional<PontuacaoCliente> findByClienteIdAndProdutoIdAndCampanhaId(Long clienteId, Long produtoId, Long campanhaId);

    @Query("""
        SELECT p FROM PontuacaoCliente p
        JOIN FETCH p.cliente
        JOIN FETCH p.produto
        JOIN FETCH p.campanha
        WHERE p.cliente.id = :clienteId
        ORDER BY p.dataCadastro DESC
        """)
    List<PontuacaoCliente> findByClienteIdComRelacionamentos(@Param("clienteId") Long clienteId);

    @Query("""
        SELECT p FROM PontuacaoCliente p
        JOIN FETCH p.cliente
        JOIN FETCH p.produto
        JOIN FETCH p.campanha
        WHERE p.cliente.id = :clienteId
        ORDER BY p.dataCadastro DESC
        """)
    Page<PontuacaoCliente> findByClienteIdComRelacionamentos(@Param("clienteId") Long clienteId, Pageable pageable);
}