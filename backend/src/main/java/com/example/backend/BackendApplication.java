package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Point d'entrée de l'application StockMaster.
 *
 * @EnableScheduling → active Spring Scheduler (AlerteScheduler)
 * @EnableAsync      → active l'exécution asynchrone (@Async sur AlerteMailService)
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
