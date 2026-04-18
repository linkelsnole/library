package org.example.repository;

import com.google.inject.Inject;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.example.entity.Book;
import org.example.persistence.Transactions;

import java.util.List;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BookRepository {

    private final EntityManagerFactory emf;

    public List<Book> findAll() {
        return Transactions.read(emf, em ->
            em.createQuery("SELECT b FROM Book b", Book.class).getResultList());
    }

    public Book findById(Long id) {
        return Transactions.read(emf, em -> em.find(Book.class, id));
    }

    public void save(Book book) {
        Transactions.write(emf, em -> {
            em.persist(book);
            return null;
        });
    }

    public Book update(Book book) {
        return Transactions.write(emf, em -> em.merge(book));
    }

    public void delete(Long id) {
        Transactions.write(emf, em -> {
            Book book = em.find(Book.class, id);
            if (book != null) {
                em.remove(book);
            }
            return null;
        });
    }
}
