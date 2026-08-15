package com.srikanth.hellofsd.web;

import com.srikanth.hellofsd.domain.Greeting;
import com.srikanth.hellofsd.service.GreetingService;
import com.srikanth.hellofsd.web.dto.GreetingRequest;
import com.srikanth.hellofsd.web.dto.GreetingResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST API for greetings. Kept intentionally small ("hello world" scope) but
 * follows real-world conventions: DTOs at the boundary, validation, proper
 * HTTP status codes, and a 201 Location header on create.
 */
@RestController
@RequestMapping("/api/v1/greetings")
public class GreetingController {

    private final GreetingService service;

    public GreetingController(GreetingService service) {
        this.service = service;
    }

    @GetMapping
    public List<GreetingResponse> getAll() {
        return service.findAll().stream().map(GreetingResponse::from).toList();
    }

    @GetMapping("/{id}")
    public GreetingResponse getOne(@PathVariable Long id) {
        return GreetingResponse.from(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<GreetingResponse> create(@Valid @RequestBody GreetingRequest request) {
        Greeting created = service.create(request);
        GreetingResponse body = GreetingResponse.from(created);
        return ResponseEntity.created(URI.create("/api/v1/greetings/" + created.getId())).body(body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deleteById(id);
    }
}
