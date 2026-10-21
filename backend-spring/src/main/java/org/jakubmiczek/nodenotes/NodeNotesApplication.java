package org.jakubmiczek.nodenotes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaAuditing
@SpringBootApplication
public class NodeNotesApplication {

    public static void main(String[] args) {
        SpringApplication.run(NodeNotesApplication.class, args);
    }

}
