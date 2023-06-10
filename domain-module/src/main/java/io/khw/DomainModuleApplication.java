package io.khw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"io.khw.common*"})
@SpringBootApplication
public class DomainModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(DomainModuleApplication.class, args);
    }

}
