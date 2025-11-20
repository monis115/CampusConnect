package com.cc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cc.modals.DocPerson;
import com.cc.repository.DocPersonRepository;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class DocPersonService {

    @Autowired
    private DocPersonRepository repository;

    @Autowired
    private UserService userService;

    public DocPerson create(DocPerson entity) {
        DocPerson savedEntity = repository.save(entity);

        com.cc.modals.User user = new com.cc.modals.User();
        user.setUsername(savedEntity.getUsername());
        user.setPassword(savedEntity.getPassword());
        user.setRole("doc_manager");
        userService.createUser(user);

        return savedEntity;
    }

    public List<DocPerson> getAll() {
        return repository.findAll();
    }

    public Optional<DocPerson> getById(Long id) {
        return repository.findById(id);
    }

    public java.util.Optional<DocPerson> getOne() {
        return repository.findAll().stream().findFirst();
    }

    public java.util.Optional<DocPerson> getByUsername(String username) {
        return repository.findByUsername(username);
    }

    public DocPerson update(Long id, DocPerson entity) {
        if (repository.existsById(id)) {
            entity.setId(id);
            return repository.save(entity);
        }
        return null;
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
