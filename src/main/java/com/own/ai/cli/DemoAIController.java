package com.own.ai.cli;

import org.springframework.ai.client.AiClient;
import org.springframework.ai.client.AiResponse;
import org.springframework.ai.client.Generation;
import org.springframework.ai.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@Component
public class DemoAIController {

    private final AiClient aiClient;
    @Value("${openai.api-key}")
    private String apiKey;

    public DemoAIController(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    public String getJoke(String topic) {
        PromptTemplate promptTemplate = new PromptTemplate("""
                I'm bored with hello world apps. How about you give me a joke about {topic}? to get started?
                Include some programming terms in your joke to make it more fun.
                """);
        promptTemplate.add("topic", topic);
        return this.aiClient.generate(promptTemplate.create()).getGeneration().getText();
    }

    public String getBestMovie(String genre, String year) {
        PromptTemplate promptTemplate = new PromptTemplate("""
                I'm bored with hello world apps. How about you give me a movie about {genre} in {year} to get started?
                But pick the best movie you can think of. I'm a movie critic, after all. IMDB ratings are a good place to start.
                And which actor or actress stars in it? And who directed it? And who wrote it? Can you give me a short plot summary and also it's name?
                But don't give me too much information. I want to be surprised.
                And please give me these details in the following JSON format: genre, year, movieName, actor, director, writer, plot.
                """);
        Map.of("genre", genre, "year", year).forEach(promptTemplate::add);
        AiResponse generate = this.aiClient.generate(promptTemplate.create());
        return generate.getGeneration().getText();
    }

    public ResponseEntity<InputStreamResource> getImage(String topic) throws URISyntaxException {
        PromptTemplate promptTemplate = new PromptTemplate("""
                I'm bored with hello world apps. Can you create me a prompt about {topic}. Enhance the topic I gave you. Make it fancy.
                Make resolution 256x256 but in Json it needs to be string. I want only 1 creation. Give me as JSON format: prompt, n, size.
                Do not make any comments. Just JSON file.
                """);
        promptTemplate.add("topic", topic);
        String imagePrompt = this.aiClient.generate(promptTemplate.create()).getGeneration().getText();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + apiKey);
        headers.add("Content-Type", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity<>(imagePrompt,headers);

        String imageUrl = restTemplate.exchange("https://api.openai.com/v1/images/generations", HttpMethod.POST, httpEntity, GeneratedImage.class)
                .getBody().getData().get(0).getUrl();
        byte[] imageBytes = restTemplate.getForObject(new URI(imageUrl), byte[].class);
        return ResponseEntity.ok().body(new InputStreamResource(new java.io.ByteArrayInputStream(imageBytes)));
    }

    //Add cache
    public String generateControllerCode(String model) {
        PromptTemplate promptTemplate = new PromptTemplate("""
                Generate a spring data rest repository for this model: {model}
                Identify the `save` method explicitly in the repository
                Generate the model as well.
                We use data jpa, so don't forget to add auto generated id and @Entity annotation to our model
                Use Lombok @Data annotation
                Use the next package name pattern 'package com.aigenerated.modelname.repositories' for repositories
                Use the next package name pattern 'package com.aigenerated.modelname.model' for model
                Please leave only the code without any additional text.
                Wrap code fragments into [java] [/java] tags
                """);
        //+Ask to use spring data rest specifications
        //+Ask to generate model code as well
        //+Use lombok
        //Ask to cover the controller byt tests using mock mvc

        promptTemplate.add("model", model);
        Generation generation = this.aiClient.generate(promptTemplate.create()).getGeneration();
        return generation.getText();
    }
}
