package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.cache;

import com.be_nuy_laundry.be_nuy_laundry.common.constant.CacheKey;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserSessionModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionCacheService {

    private final RedisCacheService redisCacheService;

    @Value("${redis.ttl.session}")
    private Long sessionTtl;

    private String buildKey(String sessionId) {
        return CacheKey.SESSION + sessionId;
    }

    // SET
    public void save(UserSessionModel session) {
        redisCacheService.set(buildKey(session.getId()), session, sessionTtl);
    }

    // GET
    public Optional<UserSessionModel> get(String sessionId) {
        return redisCacheService.get(buildKey(sessionId), UserSessionModel.class);
    }

    public void delete(String key) {
        redisCacheService.delete(buildKey(key));
    }

}
