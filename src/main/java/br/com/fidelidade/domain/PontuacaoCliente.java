package br.com.fidelidade.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "pontuacoes_clientes",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_pontuacao_cliente_produto_campanha",
        columnNames = {"cliente_id", "produto_id", "campanha_id"}
    )
)
public class PontuacaoCliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "campanha_id", nullable = false)
    private Campanha campanha;

    @Column(nullable = false)
    private Integer quantidade = 0;

    @Column(nullable = false)
    private LocalDateTime dataCadastro = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime dataAtualizacao = LocalDateTime.now();

    protected PontuacaoCliente() {}

    public PontuacaoCliente(Cliente cliente, Produto produto, Campanha campanha) {
        this.cliente = cliente;
        this.produto = produto;
        this.campanha = campanha;
        this.quantidade = 1;
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Produto getProduto() { return produto; }
    public Campanha getCampanha() { return campanha; }
    public Integer getQuantidade() { return quantidade; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }

    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public void setCampanha(Campanha campanha) { this.campanha = campanha; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}
