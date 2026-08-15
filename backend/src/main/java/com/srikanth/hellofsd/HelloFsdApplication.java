package com.srikanth.hellofsd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the hello-world Full Stack Developer demo microservice.
 *
 * This service exists to demonstrate, end-to-end, the core skills listed in a
 * typical "Full Stack Developer (Java, Microservices, Spring Boot, API, ReactJS)"
 * typical full-stack role: a Spring Boot REST API backed by a relational database, exercised
 * by a ReactJS/Redux/TypeScript frontend, containerized, deployed to Kubernetes,
 * and built/tested through a CI/CD pipeline.
 */
@SpringBootApplication
public class HelloFsdApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelloFsdApplication.class, args);
    }
}
