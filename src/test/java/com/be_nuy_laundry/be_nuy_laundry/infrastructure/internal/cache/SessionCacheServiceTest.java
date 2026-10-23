package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.cache;

import com.be_nuy_laundry.be_nuy_laundry.common.constant.CacheKey;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserSessionModel;
import com.be_nuy_laundry.be_nuy_laundry.fixture.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionCacheServiceTest {

    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    private SessionCacheService sessionCacheService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sessionCacheService, "sessionTtl", 604800L);
    }

    @Test
    @DisplayName("givenUserSessionModel_whenSave_thenDelegateToRedisCacheServiceWithPrefixedKeyAndTtl")
    void givenUserSessionModel_whenSave_thenDelegateToRedisCacheServiceWithPrefixedKeyAndTtl() {
        UserSessionModel session = TestDataBuilder.createSampleSession();

        sessionCacheService.save(session);

        verify(redisCacheService).set(CacheKey.SESSION + session.getId(), session, 604800L);
    }

    @Test
    @DisplayName("givenSessionId_whenGet_thenReturnSessionFromRedis")
    void givenSessionId_whenGet_thenReturnSessionFromRedis() {
        UserSessionModel session = TestDataBuilder.createSampleSession();
        String sessionId = session.getId();
        when(redisCacheService.get(CacheKey.SESSION + sessionId, UserSessionModel.class))
                .thenReturn(Optional.of(session));

        Optional<UserSessionModel> result = sessionCacheService.get(sessionId);

        assertThat(result).isPresent().contains(session);
    }

    @Test
    @DisplayName("givenKey_whenDelete_thenDelegateToRedisCacheServiceWithPrefixedKey")
    void givenKey_whenDelete_thenDelegateToRedisCacheServiceWithPrefixedKey() {
        String sessionId = "sess-to-delete";

        sessionCacheService.delete(sessionId);

        verify(redisCacheService).delete(CacheKey.SESSION + sessionId);
    }
}
