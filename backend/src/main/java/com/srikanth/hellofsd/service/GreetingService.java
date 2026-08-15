package com.srikanth.hellofsd.service;

import com.srikanth.hellofsd.domain.Greeting;
import com.srikanth.hellofsd.exception.ResourceNotFoundException;
import com.srikanth.hellofsd.repository.GreetingRepository;
import com.srikanth.hellofsd.web.dto.GreetingRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GreetingService {

    private final GreetingRepository repository;

    public GreetingService(GreetingRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Greeting> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Greeting findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Greeting %d not found".formatted(id)));
    }

    @Transactional
    public Greeting create(GreetingRequest request) {
        Greeting greeting = new Greeting(request.author(), request.message());
        return repository.save(greeting);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Greeting %d not found".formatted(id));
        }
        repository.deleteById(id);
    }
}
