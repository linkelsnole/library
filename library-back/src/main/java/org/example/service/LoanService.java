package org.example.service;

import com.google.inject.Inject;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.example.entity.Book;
import org.example.entity.Loan;
import org.example.entity.Reader;
import org.example.exception.BusinessException;
import org.example.exception.NotFoundException;
import org.example.persistence.Transactions;
import org.example.repository.LoanRepository;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class LoanService {

    private final LoanRepository loanRepo;
    private final EntityManagerFactory emf;

    public List<Loan> findAll() {
        return loanRepo.findAll();
    }

    public Loan issue(Long bookId, Long readerId) {
        return Transactions.write(emf, em -> {
            Book book = em.find(Book.class, bookId);

            if (book == null) {
                throw new NotFoundException("Book not found");
            }

            if (!book.isAvailable()) {
                throw new BusinessException("Book is not available");
            }
            
            Reader reader = em.find(Reader.class, readerId);
            if (reader == null) {
                throw new NotFoundException("Reader not found");
            }

            book.setAvailable(false);
            Loan loan = Loan.builder()
                    .book(book)
                    .reader(reader)
                    .takenAt(LocalDate.now())
                    .build();
            em.persist(loan);
            return loan;
        });
    }

    public Loan returnBook(Long loanId) {
        return Transactions.write(emf, em -> {
            Loan loan = em.find(Loan.class, loanId);

            if (loan == null) {
                throw new NotFoundException("Loan not found");
            }

            loan.setReturnedAt(LocalDate.now());
            loan.getBook().setAvailable(true);

            return loan;
        });
    }

    public long countByReaderAndPeriod(Long readerId, LocalDate from, LocalDate to) {
        return loanRepo.findByReaderAndPeriod(readerId, from, to).size();
    }
}
