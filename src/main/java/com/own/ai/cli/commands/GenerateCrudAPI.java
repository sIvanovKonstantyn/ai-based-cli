package com.own.ai.cli.commands;

import com.own.ai.cli.DemoAIController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class GenerateCrudAPI implements Command {

    @Autowired
    private DemoAIController aiController;

    /*


     * */
    @Override
    public void execute() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Please enter the model description");
            String model = reader.readLine();
            System.out.println("generating...");
            String response = aiController.generateControllerCode(model);

            List<ClassDescription> classDescriptions = parseClassDescriptionsFromResponse(response);
            Set<String> directories = new HashSet<>();

            for (ClassDescription classDescription : classDescriptions) {
                directories.add(classDescription.folder());
            }

            for (String dir : directories) {
                Files.createDirectories(Path.of(dir));
            }

            for (ClassDescription classDescription : classDescriptions) {
                Files.createFile(Path.of(classDescription.fileName()));
                try (BufferedWriter br = new BufferedWriter(new FileWriter(classDescription.fileName()))) {
                    br.write(classDescription.fileText());
                    br.flush();
                }
            }


        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        System.out.println("command finished the execution. Check the 'generated-output' folder");
    }

    private String mockResponse() {
        return """
                / Model
                [java]
                package com.aigenerated.modelname.model;
                                
                import javax.persistence.Entity;
                import javax.persistence.GeneratedValue;
                import javax.persistence.GenerationType;
                import javax.persistence.Id;
                                
                @Entity
                public class User {
                    @Id
                    @GeneratedValue(strategy = GenerationType.IDENTITY)
                    private Long id;
                                
                    private String name;
                                
                    // Getters and setters (or lombok annotations) here
                }
                [/java]
                                
                // Repository
                [java]
                package com.aigenerated.modelname.repositories;
                                
                import org.springframework.data.repository.PagingAndSortingRepository;
                import org.springframework.data.rest.core.annotation.RepositoryRestResource;
                                
                @RepositoryRestResource(collectionResourceRel = "users", path = "users")
                public interface UserRepository extends PagingAndSortingRepository<User, Long> {
                }
                [/java]
                """;
    }

    private List<ClassDescription> parseClassDescriptionsFromResponse(String response) {

        String[] fileTexts = response.split("\\[java]|\\[/java]");
        List<ClassDescription> result = new ArrayList<>();

        for (String fileText : fileTexts) {
            if (!fileText.contains("class") && !fileText.contains("interface")) {
                continue;
            }
            String folderName = parseFolderName(fileText);
            String fileName = folderName + "\\" + parseFileName(fileText);
            result.add(new ClassDescription(folderName, fileName, fileText));
        }

        return result;
    }

    private String parseFileName(String fileText) {
        int start = fileText.indexOf("public class ");
        if (start == -1) {
            start = fileText.indexOf("public interface ");
            start += "public interface ".length();
        } else {
            start += "public class ".length();
        }

        int end;
        if (fileText.contains("extends")) {
            end = fileText.indexOf("extends");
        } else if (fileText.contains("implements")) {
            end = fileText.indexOf("implements");
        } else {
            end = fileText.indexOf("{");
        }

        return fileText.substring(start, end).trim() + ".java";
    }

    private String parseFolderName(String fileText) {
        int start = fileText.indexOf("package ") + "package ".length();
        int end = fileText.indexOf(";");

        return "generated-output\\" + fileText.substring(start, end).trim().replaceAll("\\.", "\\\\");
    }

    private record ClassDescription(String folder, String fileName, String fileText) {
    }
}
