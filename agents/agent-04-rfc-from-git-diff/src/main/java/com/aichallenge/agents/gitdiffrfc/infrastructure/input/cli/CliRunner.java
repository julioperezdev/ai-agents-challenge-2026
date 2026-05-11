package com.aichallenge.agents.gitdiffrfc.infrastructure.input.cli;

import com.aichallenge.agents.gitdiffrfc.application.GenerateRfcRequest;
import com.aichallenge.agents.gitdiffrfc.application.GitDiffRfcGenerator;
import java.nio.file.Files;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class CliRunner implements CommandLineRunner {

    private final CliArgumentsParser parser;
    private final GitDiffRfcGenerator generator;
    private final ConfigurableApplicationContext context;

    public CliRunner(CliArgumentsParser parser, GitDiffRfcGenerator generator, ConfigurableApplicationContext context) {
        this.parser = parser;
        this.generator = generator;
        this.context = context;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            if (args.length == 0 || containsHelp(args)) {
                System.out.println(parser.usage());
                close(0);
                return;
            }

            GenerateRfcRequest request = parser.parse(args);
            String markdown = generator.generate(request);
            if (request.output().isPresent()) {
                Files.writeString(request.output().get(), markdown);
                System.out.println("RFC written to " + request.output().get().toAbsolutePath());
            } else {
                System.out.println(markdown);
            }
            close(0);
        } catch (IllegalArgumentException ex) {
            System.err.println("Invalid arguments: " + ex.getMessage());
            System.err.println();
            System.err.println(parser.usage());
            close(2);
        } catch (RuntimeException ex) {
            System.err.println("Execution failed: " + ex.getMessage());
            close(1);
        }
    }

    private boolean containsHelp(String[] args) {
        for (String arg : args) {
            if ("--help".equals(arg) || "-h".equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private void close(int code) {
        int exitCode = org.springframework.boot.SpringApplication.exit(context, () -> code);
        System.exit(exitCode);
    }
}
