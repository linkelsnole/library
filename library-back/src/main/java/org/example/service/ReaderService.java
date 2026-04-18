package org.example.service;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.example.entity.Reader;
import org.example.exception.NotFoundException;
import org.example.repository.ReaderRepository;

import java.util.List;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ReaderService {

    private final ReaderRepository repo;

    public List<Reader> findAll() {
        return repo.findAll();
    }

    public Reader findById(Long id) {
        Reader r = repo.findById(id);
        if (r == null) {
            throw new NotFoundException("Reader not found: " + id);
        }
        return r;
    }

    public Reader save(Reader reader) {
        repo.save(reader);
        return reader;
    }

    public Reader update(Long id, Reader incoming) {
        Reader existing = findById(id);
        existing.setFullName(incoming.getFullName());
        existing.setGender(incoming.getGender());
        existing.setAge(incoming.getAge());
        return repo.update(existing);
    }

    public void delete(Long id) {
        repo.delete(id);
    }
}
