package com.cc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cc.modals.VisaMan;
import com.cc.repository.VisaManRepository;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class VisaManService {

    @Autowired
    private VisaManRepository repository;

    @Autowired
    private UserService userService;

    public VisaMan create(VisaMan entity) {
        VisaMan savedEntity = repository.save(entity);

        com.cc.modals.User user = new com.cc.modals.User();
        user.setUsername(savedEntity.getUsername());
        user.setPassword(savedEntity.getPassword());
        user.setRole("visa_manager");
        userService.createUser(user);

        return savedEntity;
    }

    public List<VisaMan> getAll() {
        return repository.findAll();
    }

    public Optional<VisaMan> getById(Long id) {
        return repository.findById(id);
    }

    public java.util.Optional<VisaMan> getOne() {
        return repository.findAll().stream().findFirst();
    }

    public Optional<VisaMan> getByUsername(String username) {
        return repository.findByUsername(username);
    }

    public VisaMan update(Long id, VisaMan entity) {
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
