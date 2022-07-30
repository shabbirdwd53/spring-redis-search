package com.dailycodebuffer.springredissearch;

import com.dailycodebuffer.springredissearch.model.Post;
import com.dailycodebuffer.springredissearch.repository.PostRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.search.FieldName;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Schema;

import java.util.Arrays;

@SpringBootApplication
public class SpringRedisSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringRedisSearchApplication.class, args);
    }

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UnifiedJedis jedis;

    @Value("classpath:data.json")
    Resource resourceFile;

    @Bean
    CommandLineRunner loadData() {
        return args -> {
            postRepository.deleteAll();

            try {
                jedis.ftDropIndex("post-idx");
            } catch (Exception e) {
                System.out.println("Index is not available");
            }

            String data = new String(resourceFile
                    .getInputStream()
                    .readAllBytes());

            ObjectMapper objectMapper
                    = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            Post[] posts =
                    objectMapper.readValue(data, Post[].class);
            Arrays.stream(posts).forEach(postRepository::save);

            Schema schema = new Schema()
                    .addField(new Schema.Field(FieldName.of("$.content").as("content"), Schema.FieldType.TEXT,true,false))
                    .addField(new Schema.TextField(FieldName.of("$.title").as("title")))
                    .addField(new Schema.Field(FieldName.of("$.tags[*]").as("tags"), Schema.FieldType.TAG))
                    .addField(new Schema.Field(FieldName.of("$.views").as("views"), Schema.FieldType.NUMERIC,false,true));

            IndexDefinition indexDefinition
                    = new IndexDefinition(IndexDefinition.Type.JSON)
                    .setPrefixes(new String[] {"post:"});

            jedis.ftCreate("post-idx",
                    IndexOptions.defaultOptions().setDefinition(indexDefinition),
                    schema);
        };
    }
}
