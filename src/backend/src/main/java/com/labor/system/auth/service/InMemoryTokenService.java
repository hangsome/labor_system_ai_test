package com.labor.system.auth.service;

import com.labor.system.auth.model.TokenPair;
import com.labor.system.auth.model.TokenPrincipal;
import com.labor.system.common.api.AppException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class InMemoryTokenService {

  private static final Duration ACCESS_TOKEN_TTL = Duration.ofHours(1);
  private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);
  private static final int TOKEN_BYTES = 32;

  private final SecureRandom secureRandom = new SecureRandom();
  private final Map<String, TokenRecord> accessTokenStore = new ConcurrentHashMap<>();
  private final Map<String, TokenRecord> refreshTokenStore = new ConcurrentHashMap<>();
  private final Clock clock;

  public InMemoryTokenService() {
    this(Clock.systemUTC());
  }

  InMemoryTokenService(Clock clock) {
    this.clock = clock;
  }

  public TokenPair issueTokens(Long userId, String username) {
    purgeExpiredTokens();
    String accessToken = newToken();
    String refreshToken = newToken();
    Instant now = clock.instant();
    accessTokenStore.put(accessToken, new TokenRecord(userId, username, now.plus(ACCESS_TOKEN_TTL)));
    refreshTokenStore.put(refreshToken, new TokenRecord(userId, username, now.plus(REFRESH_TOKEN_TTL)));
    return new TokenPair(accessToken, refreshToken, ACCESS_TOKEN_TTL.toSeconds());
  }

  public TokenPair refreshAccessToken(String refreshToken) {
    purgeExpiredTokens();
    TokenRecord refreshRecord = refreshTokenStore.get(refreshToken);
    if (refreshRecord == null || refreshRecord.expiresAt().isBefore(clock.instant())) {
      refreshTokenStore.remove(refreshToken);
      throw AppException.unauthorized("未认证或令牌失效");
    }
    String newAccessToken = newToken();
    accessTokenStore.put(
        newAccessToken,
        new TokenRecord(
            refreshRecord.userId(), refreshRecord.username(), clock.instant().plus(ACCESS_TOKEN_TTL)));
    return new TokenPair(newAccessToken, refreshToken, ACCESS_TOKEN_TTL.toSeconds());
  }

  public TokenPrincipal parseAccessToken(String accessToken) {
    purgeExpiredTokens();
    TokenRecord accessRecord = accessTokenStore.get(accessToken);
    if (accessRecord == null || accessRecord.expiresAt().isBefore(clock.instant())) {
      accessTokenStore.remove(accessToken);
      throw AppException.unauthorized("未认证或令牌失效");
    }
    return new TokenPrincipal(accessRecord.userId(), accessRecord.username());
  }

  private String newToken() {
    byte[] bytes = new byte[TOKEN_BYTES];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private void purgeExpiredTokens() {
    Instant now = clock.instant();
    accessTokenStore.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    refreshTokenStore.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
  }

  private record TokenRecord(Long userId, String username, Instant expiresAt) {}
}

