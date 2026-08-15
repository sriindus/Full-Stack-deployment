package com.srikanth.hellofsd.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srikanth.hellofsd.repository.GreetingRepository;
import com.srikanth.hellofsd.web.dto.GreetingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack (of the backend, at least) integration test: real Spring context,
 * real HTTP request dispatch via MockMvc, real JPA/H2 persistence. This is the
 * "integration test" layer called out in a typical layered testing strategy.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GreetingControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GreetingRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void createThenListThenGet_roundTripsThroughRealDatabase() throws Exception {
        GreetingRequest request = new GreetingRequest("Srikanth", "Hello from the integration test!");

        String createdBody = mockMvc.perform(post("/api/v1/greetings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.author", is("Srikanth")))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createdBody).get("id").asLong();

        mockMvc.perform(get("/api/v1/greetings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].message", is("Hello from the integration test!")));

        mockMvc.perform(get("/api/v1/greetings/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author", is("Srikanth")));
    }

    @Test
    void get_returns404_whenGreetingDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/greetings/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void create_returns400_whenPayloadInvalid() throws Exception {
        String invalidJson = """
                {"author": "", "message": ""}
                """;

        mockMvc.perform(post("/api/v1/greetings")
                        .contentType("application/json")
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.author", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors.message", notNullValue()));
    }

    @Test
    void delete_removesGreeting() throws Exception {
        String createdBody = mockMvc.perform(post("/api/v1/greetings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new GreetingRequest("Temp", "to be deleted"))))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(createdBody).get("id").asLong();

        mockMvc.perform(delete("/api/v1/greetings/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/greetings/{id}", id))
                .andExpect(status().isNotFound());
    }
}
