package br.com.fidelidade.service;

import br.com.fidelidade.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClienteServiceTest {
    @Test void rejeitaEmailInvalido() { var repo = mock(ClienteRepository.class); var service = new ClienteService(repo); assertThrows(IllegalArgumentException.class, () -> service.cadastrar("Ana", "invalido", "", null)); verifyNoInteractions(repo); }
    @Test void rejeitaDuplicidade() { var repo = mock(ClienteRepository.class); when(repo.existsByEmailIgnoreCase("ana@email.com")).thenReturn(true); var service = new ClienteService(repo); assertThrows(IllegalArgumentException.class, () -> service.cadastrar("Ana", "ana@email.com", "", null)); }
}

