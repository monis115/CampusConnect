package com.cc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cc.modals.TelexMan;
import com.cc.repository.TelexManRepository;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class TelexManService {

    @Autowired
    private TelexManRepository repository;

    @Autowired
    private UserService userService;

    public TelexMan create(TelexMan entity) {
        TelexMan savedEntity = repository.save(entity);

        com.cc.modals.User user = new com.cc.modals.User();
        user.setUsername(savedEntity.getUsername());
        user.setPassword(savedEntity.getPassword());
        user.setRole("telex_manager");
        userService.createUser(user);

        return savedEntity;
    }

    public List<TelexMan> getAll() {
        return repository.findAll();
    }

    public Optional<TelexMan> getById(Long id) {
        return repository.findById(id);
    }

    public java.util.Optional<TelexMan> getOne() {
        return repository.findAll().stream().findFirst();
    }

    public Optional<TelexMan> getByUsername(String username) {
        return repository.findByUsername(username);
    }

    public TelexMan update(Long id, TelexMan entity) {
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
