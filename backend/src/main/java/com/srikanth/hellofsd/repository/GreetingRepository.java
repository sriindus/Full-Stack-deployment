package com.srikanth.hellofsd.repository;

import com.srikanth.hellofsd.domain.Greeting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GreetingRepository extends JpaRepository<Greeting, Long> {
}
