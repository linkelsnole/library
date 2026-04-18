package org.example.service;

import org.example.entity.Reader;
import org.example.exception.NotFoundException;
import org.example.repository.ReaderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReaderServiceTest {

    @Mock
    private ReaderRepository repo;

    @InjectMocks
    private ReaderService service;

    @Test
    void findAll_returnsList() {
        Reader r = Reader.builder()
                .fullName("Ivan")
                .build();
        when(repo.findAll()).thenReturn(List.of(r));

        List<Reader> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals("Ivan", result.get(0).getFullName());
    }

    @Test
    void findById_found_returns() {
        Reader r = Reader.builder()
                .id(1L)
                .build();
        when(repo.findById(1L)).thenReturn(r);

        assertEquals(1L, service.findById(1L).getId());
    }

    @Test
    void findById_notFound_throws() {
        when(repo.findById(99L)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void save_callsRepo() {
        Reader r = Reader.builder()
                .fullName("Petr")
                .build();

        Reader result = service.save(r);

        verify(repo).save(r);
        assertEquals("Petr", result.getFullName());
    }

    @Test
    void update_overwritesFields() {
        Reader existing = Reader.builder()
                .id(1L)
                .fullName("Old")
                .age(20)
                .build();

        Reader incoming = Reader.builder()
                .fullName("New")
                .gender("M")
                .age(30)
                .build();

        when(repo.findById(1L)).thenReturn(existing);
        when(repo.update(any())).thenAnswer(inv -> inv.getArgument(0));

        Reader result = service.update(1L, incoming);

        assertEquals("New", result.getFullName());
        assertEquals("M", result.getGender());
        assertEquals(30, result.getAge());
        verify(repo).update(existing);
    }

    @Test
    void update_notFound_throws() {
        when(repo.findById(42L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.update(42L, Reader.builder().build()));
        verify(repo, never()).update(any());
    }

    @Test
    void delete_callsRepo() {
        service.delete(7L);
        verify(repo).delete(7L);
    }
}
