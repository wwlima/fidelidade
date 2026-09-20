package br.com.fidelidade.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "clientes", uniqueConstraints = @UniqueConstraint(name = "uk_cliente_email", columnNames = "email"))
public class Cliente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120) private String nome;
    @Column(nullable = false, length = 180) private String email;
    @Column(length = 30) private String telefone;
    @Column private LocalDate dataNascimento;
    @Column(nullable = false) private Integer pontos = 0;
    @Column(nullable = false) private LocalDateTime cadastradoEm = LocalDateTime.now();

    protected Cliente() {}
    public Cliente(String nome, String email, String telefone, LocalDate dataNascimento) { 
        this.nome = nome; 
        this.email = email; 
        this.telefone = telefone; 
        this.dataNascimento = dataNascimento;
    }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getTelefone() { return telefone; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public Integer getPontos() { return pontos; }
    public LocalDateTime getCadastradoEm() { return cadastradoEm; }

    public void setNome(String nome) { this.nome = nome; }
    public void setEmail(String email) { this.email = email; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
}