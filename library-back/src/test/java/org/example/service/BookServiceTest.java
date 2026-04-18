package org.example.service;

import org.example.entity.Book;
import org.example.exception.NotFoundException;
import org.example.repository.BookRepository;
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
class BookServiceTest {

    @Mock
    private BookRepository repo;

    @InjectMocks
    private BookService service;

    @Test
    void findAll_returnsList() {
        Book b = Book.builder()
                .title("Test")
                .build();
        when(repo.findAll()).thenReturn(List.of(b));

        List<Book> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).getTitle());
    }

    @Test
    void findById_found_returnsBook() {
        Book b = Book.builder()
                .id(1L)
                .build();
        when(repo.findById(1L)).thenReturn(b);

        Book result = service.findById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void findById_notFound_throws() {
        when(repo.findById(99L)).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.findById(99L));
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void save_callsRepo() {
        Book b = Book.builder()
                .title("New")
                .build();

        Book result = service.save(b);

        verify(repo).save(b);
        assertEquals("New", result.getTitle());
    }

    @Test
    void update_overwritesFields() {
        Book existing = Book.builder()
                .id(1L)
                .title("Old")
                .author("Old Author")
                .build();

        Book incoming = Book.builder()
                .title("New")
                .author("New Author")
                .year(2020)
                .isbn("123")
                .build();

        when(repo.findById(1L)).thenReturn(existing);
        when(repo.update(any())).thenAnswer(inv -> inv.getArgument(0));

        Book result = service.update(1L, incoming);

        assertEquals("New", result.getTitle());
        assertEquals("New Author", result.getAuthor());
        assertEquals(2020, result.getYear());
        assertEquals("123", result.getIsbn());
        verify(repo).update(existing);
    }

    @Test
    void update_notFound_throws() {
        when(repo.findById(42L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.update(42L, Book.builder().build()));
        verify(repo, never()).update(any());
    }

    @Test
    void delete_callsRepo() {
        service.delete(5L);
        verify(repo).delete(5L);
    }
}
