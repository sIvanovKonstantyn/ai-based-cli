package com.own.ai.cli;

import com.own.ai.cli.commands.Command;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
@Slf4j
public class AiCliToolApplication implements CommandLineRunner {

    @Autowired
    private Command generateCrudAPICommand;

    public static void main(String[] args) {
        SpringApplication.run(AiCliToolApplication.class, args);
    }

    @Override
    public void run(String... args) throws IOException {
        String currentPath = new java.io.File(".").getCanonicalPath();
        System.out.println("Current dir:" + currentPath);
        System.out.println("CLI helper is ready to work. Please use '-help' to see the functions");

        for (int i = 0; i < args.length; ++i) {
            log.info("args[{}]: {}", i, args[i]);
        }


        try (BufferedReader reader = new BufferedReader(new InputStreamReader( System.in))){

            String option = reader.readLine();
            while (!option.equals("exit")) {
                if (option.equals("-help")) {
                    System.out.println("--generate folder - generates the test folder");
                    System.out.println("--generate controller (-gafm) - generates spring rest controller with CRUD operation for specific model (USING AI)");
                    System.out.println("--exit - exits the CLI");
                } else if (option.equals("--generate folder")) {
                    Files.createDirectory(Path.of("test-folder"));
                } else if (option.equals("--generate API for model") || option.equals("-gafm")) {
                    generateCrudAPICommand.execute();
                }
                option = reader.readLine();
            }
       }
    }
}