package org.example.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.example.entity.Book;
import org.example.entity.Loan;
import org.example.entity.Reader;
import org.example.exception.BusinessException;
import org.example.exception.NotFoundException;
import org.example.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepo;

    @Mock
    private EntityManagerFactory emf;

    @Mock
    private EntityManager em;

    @Mock
    private EntityTransaction tx;

    private LoanService service;

    @BeforeEach
    void setUp() {
        service = new LoanService(loanRepo, emf);
    }

    private void stubEmfForWrite() {
        when(emf.createEntityManager()).thenReturn(em);
        when(em.getTransaction()).thenReturn(tx);
    }

    @Test
    void findAll_returnsList() {
        Loan l = Loan.builder().build();
        when(loanRepo.findAll()).thenReturn(List.of(l));

        List<Loan> result = service.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void issue_success_persistsLoanAndMarksBookUnavailable() {
        stubEmfForWrite();

        Book book = Book.builder()
                .id(1L)
                .available(true)
                .build();

        Reader reader = Reader.builder()
                .id(2L)
                .build();

        when(em.find(Book.class, 1L)).thenReturn(book);
        when(em.find(Reader.class, 2L)).thenReturn(reader);

        Loan result = service.issue(1L, 2L);

        assertNotNull(result);
        assertEquals(book, result.getBook());
        assertEquals(reader, result.getReader());
        assertEquals(LocalDate.now(), result.getTakenAt());
        assertFalse(book.isAvailable());
        verify(em).persist(any(Loan.class));
        verify(tx).begin();
        verify(tx).commit();
    }

    @Test
    void issue_bookNotFound_throwsAndRollsBack() {
        stubEmfForWrite();
        when(em.find(Book.class, 1L)).thenReturn(null);
        when(tx.isActive()).thenReturn(true);

        assertThrows(NotFoundException.class, () -> service.issue(1L, 2L));
        verify(tx).rollback();
        verify(em, never()).persist(any());
    }

    @Test
    void issue_bookNotAvailable_throwsAndRollsBack() {
        stubEmfForWrite();

        Book book = Book.builder()
                .available(false)
                .build();
        when(em.find(Book.class, 1L)).thenReturn(book);
        when(tx.isActive()).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.issue(1L, 2L));
        verify(tx).rollback();
        verify(em, never()).persist(any());
    }

    @Test
    void issue_readerNotFound_throwsAndRollsBack() {
        stubEmfForWrite();

        Book book = Book.builder()
                .available(true)
                .build();
        when(em.find(Book.class, 1L)).thenReturn(book);
        when(em.find(Reader.class, 2L)).thenReturn(null);
        when(tx.isActive()).thenReturn(true);

        assertThrows(NotFoundException.class, () -> service.issue(1L, 2L));
        verify(tx).rollback();
    }

    @Test
    void returnBook_success_marksReturnedAndBookAvailable() {
        stubEmfForWrite();

        Book book = Book.builder()
                .available(false)
                .build();

        Loan loan = Loan.builder()
                .id(5L)
                .book(book)
                .build();

        when(em.find(Loan.class, 5L)).thenReturn(loan);

        Loan result = service.returnBook(5L);

        assertEquals(LocalDate.now(), result.getReturnedAt());
        assertTrue(book.isAvailable());
        verify(tx).commit();
    }

    @Test
    void returnBook_notFound_throwsAndRollsBack() {
        stubEmfForWrite();
        when(em.find(Loan.class, 99L)).thenReturn(null);
        when(tx.isActive()).thenReturn(true);

        assertThrows(NotFoundException.class, () -> service.returnBook(99L));
        verify(tx).rollback();
    }

    @Test
    void countByReaderAndPeriod_returnsSize() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);
        when(loanRepo.findByReaderAndPeriod(eq(1L), eq(from), eq(to)))
                .thenReturn(List.of(Loan.builder().build(), Loan.builder().build(), Loan.builder().build()));

        long count = service.countByReaderAndPeriod(1L, from, to);

        assertEquals(3, count);
    }

    @Test
    void countByReaderAndPeriod_empty_returnsZero() {
        when(loanRepo.findByReaderAndPeriod(any(), any(), any())).thenReturn(List.of());

        long count = service.countByReaderAndPeriod(1L, LocalDate.now(), LocalDate.now());

        assertEquals(0, count);
    }
}
