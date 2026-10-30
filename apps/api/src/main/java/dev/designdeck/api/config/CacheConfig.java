package dev.designdeck.api.config;

import java.time.Duration;
import java.util.Map;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.designdeck.api.dto.catalog.CategoryDto;
import dev.designdeck.api.dto.catalog.QuestionDto;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        JavaType categoriesType = objectMapper.getTypeFactory()
                .constructCollectionType(java.util.List.class, CategoryDto.class);
        JavaType questionsType = objectMapper.getTypeFactory()
                .constructCollectionType(java.util.List.class, QuestionDto.class);

        RedisCacheConfiguration defaultConfig = cacheConfig(Duration.ofMinutes(30),
                new Jackson2JsonRedisSerializer<>(objectMapper, Object.class));

        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                "categories", cacheConfig(Duration.ofHours(1),
                        new Jackson2JsonRedisSerializer<>(objectMapper, categoriesType)),
                "questions", cacheConfig(Duration.ofMinutes(30),
                        new Jackson2JsonRedisSerializer<>(objectMapper, questionsType)),
                "question", cacheConfig(Duration.ofMinutes(30),
                        new Jackson2JsonRedisSerializer<>(objectMapper, QuestionDto.class))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    private RedisCacheConfiguration cacheConfig(Duration ttl, RedisSerializer<?> valueSerializer) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .computePrefixWith(cacheName -> "designdeck:v2:" + cacheName + "::")
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));
    }
}
