package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisCacheService redisCacheService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("givenKeyValueTtl_whenSet_thenInvokeOpsForValueSetWithDuration")
    void givenKeyValueTtl_whenSet_thenInvokeOpsForValueSetWithDuration() {
        redisCacheService.set("testKey", "testVal", 3600);

        verify(valueOperations).set("testKey", "testVal", Duration.ofSeconds(3600));
    }

    @Test
    @DisplayName("givenExistingKey_whenGet_thenReturnOptionalContainingValue")
    void givenExistingKey_whenGet_thenReturnOptionalContainingValue() {
        when(valueOperations.get("testKey")).thenReturn("cachedValue");

        Optional<Object> result = redisCacheService.get("testKey");

        assertThat(result).isPresent().contains("cachedValue");
    }

    @Test
    @DisplayName("givenNonExistingKey_whenGet_thenReturnEmptyOptional")
    void givenNonExistingKey_whenGet_thenReturnEmptyOptional() {
        when(valueOperations.get("missingKey")).thenReturn(null);

        Optional<Object> result = redisCacheService.get("missingKey");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("givenKeyAndClass_whenGetWithClass_thenReturnCastOptional")
    void givenKeyAndClass_whenGetWithClass_thenReturnCastOptional() {
        when(valueOperations.get("numKey")).thenReturn(12345);

        Optional<Integer> result = redisCacheService.get("numKey", Integer.class);

        assertThat(result).isPresent().contains(12345);
    }

    @Test
    @DisplayName("givenMissingKeyAndClass_whenGetWithClass_thenReturnEmptyOptional")
    void givenMissingKeyAndClass_whenGetWithClass_thenReturnEmptyOptional() {
        when(valueOperations.get("missingKey")).thenReturn(null);

        Optional<Integer> result = redisCacheService.get("missingKey", Integer.class);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("givenKey_whenDelete_thenInvokeRedisTemplateDelete")
    void givenKey_whenDelete_thenInvokeRedisTemplateDelete() {
        redisCacheService.delete("deleteKey");

        verify(redisTemplate).delete("deleteKey");
    }

    @Test
    @DisplayName("givenKey_whenExists_thenReturnBoolean")
    void givenKey_whenExists_thenReturnBoolean() {
        when(redisTemplate.hasKey("existKey")).thenReturn(true);
        when(redisTemplate.hasKey("notExistKey")).thenReturn(false);

        assertThat(redisCacheService.exists("existKey")).isTrue();
        assertThat(redisCacheService.exists("notExistKey")).isFalse();
    }
}
