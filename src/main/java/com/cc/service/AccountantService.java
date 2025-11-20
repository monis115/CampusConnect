package com.cc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cc.modals.Accountant;
import com.cc.repository.AccountantRepository;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class AccountantService {

    @Autowired
    private AccountantRepository repository;

    @Autowired
    private UserService userService;

    public Accountant create(Accountant entity) {
        Accountant savedEntity = repository.save(entity);

        com.cc.modals.User user = new com.cc.modals.User();
        user.setUsername(savedEntity.getUsername());
        user.setPassword(savedEntity.getPassword());
        user.setRole("accountant");
        userService.createUser(user);

        return savedEntity;
    }

    public List<Accountant> getAll() {
        return repository.findAll();
    }

    public Optional<Accountant> getById(Long id) {
        return repository.findById(id);
    }

    public Optional<Accountant> getByUsername(String username) {
        return repository.findByUsername(username);
    }

    public Accountant update(Long id, Accountant entity) {
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
