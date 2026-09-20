package br.com.fidelidade.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "campanhas")
public class Campanha {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false)
    private LocalDate dataInicioVigencia;

    @Column(nullable = false)
    private LocalDate dataFimVigencia;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private LocalDateTime dataCadastro = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime dataAtualizacao = LocalDateTime.now();

    protected Campanha() {}

    public Campanha(String nome, LocalDate dataInicioVigencia, LocalDate dataFimVigencia, boolean ativo) {
        this.nome = nome;
        this.dataInicioVigencia = dataInicioVigencia;
        this.dataFimVigencia = dataFimVigencia;
        this.ativo = ativo;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public LocalDate getDataInicioVigencia() { return dataInicioVigencia; }
    public LocalDate getDataFimVigencia() { return dataFimVigencia; }
    public boolean isAtivo() { return ativo; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }

    public void setNome(String nome) { this.nome = nome; }
    public void setDataInicioVigencia(LocalDate dataInicioVigencia) { this.dataInicioVigencia = dataInicioVigencia; }
    public void setDataFimVigencia(LocalDate dataFimVigencia) { this.dataFimVigencia = dataFimVigencia; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}
