package org.example.repository;

import com.google.inject.Inject;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.example.entity.Reader;
import org.example.persistence.Transactions;

import java.util.List;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ReaderRepository {

    private final EntityManagerFactory emf;

    public List<Reader> findAll() {
        return Transactions.read(emf, em ->
            em.createQuery("SELECT r FROM Reader r", Reader.class).getResultList());
    }

    public Reader findById(Long id) {
        return Transactions.read(emf, em -> em.find(Reader.class, id));
    }

    public void save(Reader reader) {
        Transactions.write(emf, em -> {
            em.persist(reader);
            return null;
        });
    }

    public Reader update(Reader reader) {
        return Transactions.write(emf, em -> em.merge(reader));
    }

    public void delete(Long id) {
        Transactions.write(emf, em -> {
            Reader reader = em.find(Reader.class, id);
            if (reader != null) {
                em.remove(reader);
            }
            return null;
        });
    }
}
