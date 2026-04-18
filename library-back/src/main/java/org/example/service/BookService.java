package org.example.service;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.example.entity.Book;
import org.example.exception.NotFoundException;
import org.example.repository.BookRepository;

import java.util.List;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BookService {

    private final BookRepository repo;

    public List<Book> findAll() {
        return repo.findAll();
    }

    public Book findById(Long id) {
        Book b = repo.findById(id);
        if (b == null) {
            throw new NotFoundException("Book not found: " + id);
        }
        return b;
    }

    public Book save(Book book) {
        repo.save(book);
        return book;
    }

    public Book update(Long id, Book incoming) {
        Book existing = findById(id);
        existing.setTitle(incoming.getTitle());
        existing.setAuthor(incoming.getAuthor());
        existing.setYear(incoming.getYear());
        existing.setIsbn(incoming.getIsbn());
        return repo.update(existing);
    }

    public void delete(Long id) {
        repo.delete(id);
    }
}
