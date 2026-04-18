package org.example.repository;

import com.google.inject.Inject;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.example.entity.Loan;
import org.example.persistence.Transactions;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class LoanRepository {

    private final EntityManagerFactory emf;

    public List<Loan> findAll() {
        return Transactions.read(emf, em ->
            em.createQuery("SELECT l FROM Loan l", Loan.class).getResultList());
    }

    public Loan findById(Long id) {
        return Transactions.read(emf, em -> em.find(Loan.class, id));
    }

    public void save(Loan loan) {
        Transactions.write(emf, em -> {
            em.persist(loan);
            return null;
        });
    }

    public Loan update(Loan loan) {
        return Transactions.write(emf, em -> em.merge(loan));
    }

    public List<Loan> findByReaderAndPeriod(Long readerId, LocalDate from, LocalDate to) {
        return Transactions.read(emf, em ->
            em.createQuery(
                    "SELECT l FROM Loan l WHERE l.reader.id = :rid AND l.takenAt BETWEEN :from AND :to",
                    Loan.class)
                .setParameter("rid", readerId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList());
    }
}
