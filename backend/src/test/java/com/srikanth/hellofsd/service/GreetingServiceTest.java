package com.srikanth.hellofsd.service;

import com.srikanth.hellofsd.domain.Greeting;
import com.srikanth.hellofsd.exception.ResourceNotFoundException;
import com.srikanth.hellofsd.repository.GreetingRepository;
import com.srikanth.hellofsd.web.dto.GreetingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pure unit test: the repository is mocked so this exercises only
 * GreetingService's own logic, with no Spring context or database involved.
 */
@ExtendWith(MockitoExtension.class)
class GreetingServiceTest {

    @Mock
    private GreetingRepository repository;

    @InjectMocks
    private GreetingService service;

    private Greeting existing;

    @BeforeEach
    void setUp() throws Exception {
        existing = new Greeting("Srikanth", "Hello, world!");
        setId(existing, 1L);
    }

    @Test
    void findAll_returnsEverythingFromRepository() {
        when(repository.findAll()).thenReturn(List.of(existing));

        List<Greeting> result = service.findAll();

        assertThat(result).hasSize(1).containsExactly(existing);
    }

    @Test
    void findById_returnsGreeting_whenPresent() {
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        Greeting result = service.findById(1L);

        assertThat(result.getAuthor()).isEqualTo("Srikanth");
    }

    @Test
    void findById_throws_whenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_savesAndReturnsNewGreeting() {
        when(repository.save(any(Greeting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Greeting result = service.create(new GreetingRequest("Alice", "Hi there"));

        assertThat(result.getAuthor()).isEqualTo("Alice");
        assertThat(result.getMessage()).isEqualTo("Hi there");
        verify(repository, times(1)).save(any(Greeting.class));
    }

    @Test
    void deleteById_throws_whenMissing() {
        when(repository.existsById(42L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteById(42L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }

    private static void setId(Greeting greeting, Long id) throws Exception {
        Field field = Greeting.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(greeting, id);
    }
}
