package org.example.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
@UtilityClass
public class Transactions {

    public static <T> T write(EntityManagerFactory emf, Function<EntityManager, T> work) {
        try (EntityManager em = emf.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                T result = work.apply(em);
                tx.commit();
                return result;
            } catch (RuntimeException e) {
                if (tx.isActive()) {
                    log.warn("Rolling back transaction due to: {}", e.getMessage());
                    tx.rollback();
                }
                log.error("Transaction failed", e);
                throw e;
            }
        }
    }

    public static <T> T read(EntityManagerFactory emf, Function<EntityManager, T> work) {
        try (EntityManager em = emf.createEntityManager()) {
            return work.apply(em);
        }
    }
}
