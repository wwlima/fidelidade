package br.com.fidelidade.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "produtos")
public class Produto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120) private String nome;
    @Column(nullable = false) private LocalDateTime cadastradoEm = LocalDateTime.now();

    protected Produto() {}
    public Produto(String nome) { 
        this.nome = nome; 
    }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public LocalDateTime getCadastradoEm() { return cadastradoEm; }
}
