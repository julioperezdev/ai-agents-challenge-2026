package dev.julioperez.postgresmcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PostgresSchemaMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostgresSchemaMcpApplication.class, args);
    }
}
