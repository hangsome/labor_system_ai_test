package com.labor.system.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labor.system.auth.model.TokenPair;
import com.labor.system.auth.model.TokenPrincipal;
import com.labor.system.common.api.AppException;
import com.labor.system.common.api.ErrorCodes;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

  @Test
  void issueTokensShouldCreateJwtAndParseAccessToken() {
    MutableClock clock = new MutableClock(Instant.parse("2026-02-26T10:00:00Z"));
    JwtTokenService tokenService =
        new JwtTokenService(
            clock,
            new ObjectMapper(),
            "labor-system-jwt-test-secret",
            Duration.ofHours(1),
            Duration.ofDays(7));

    TokenPair tokenPair = tokenService.issueTokens(1001L, "admin");
    TokenPrincipal principal = tokenService.parseAccessToken(tokenPair.accessToken());

    assertEquals(3, tokenPair.accessToken().split("\\.").length);
    assertEquals(3, tokenPair.refreshToken().split("\\.").length);
    assertEquals(1001L, principal.userId());
    assertEquals("admin", principal.username());
  }

  @Test
  void refreshAccessTokenShouldIssueNewAccessToken() {
    MutableClock clock = new MutableClock(Instant.parse("2026-02-26T10:00:00Z"));
    JwtTokenService tokenService =
        new JwtTokenService(
            clock,
            new ObjectMapper(),
            "labor-system-jwt-test-secret",
            Duration.ofHours(1),
            Duration.ofDays(7));

    TokenPair tokenPair = tokenService.issueTokens(1002L, "manager");
    TokenPair refreshed = tokenService.refreshAccessToken(tokenPair.refreshToken());

    assertNotEquals(tokenPair.accessToken(), refreshed.accessToken());
    assertEquals(tokenPair.refreshToken(), refreshed.refreshToken());
    assertEquals(3600L, refreshed.expiresIn());
  }

  @Test
  void refreshAccessTokenShouldFailAfterRefreshTokenInvalidated() {
    MutableClock clock = new MutableClock(Instant.parse("2026-02-26T10:00:00Z"));
    JwtTokenService tokenService =
        new JwtTokenService(
            clock,
            new ObjectMapper(),
            "labor-system-jwt-test-secret",
            Duration.ofHours(1),
            Duration.ofDays(7));

    TokenPair tokenPair = tokenService.issueTokens(1003L, "auditor");
    tokenService.invalidateRefreshToken(tokenPair.refreshToken());

    AppException exception =
        assertThrows(
            AppException.class, () -> tokenService.refreshAccessToken(tokenPair.refreshToken()));
    assertEquals(ErrorCodes.AUTH_UNAUTHORIZED, exception.getCode());
  }

  @Test
  void parseAccessTokenShouldFailWhenTokenRevoked() {
    MutableClock clock = new MutableClock(Instant.parse("2026-02-26T10:00:00Z"));
    JwtTokenService tokenService =
        new JwtTokenService(
            clock,
            new ObjectMapper(),
            "labor-system-jwt-test-secret",
            Duration.ofHours(1),
            Duration.ofDays(7));

    TokenPair tokenPair = tokenService.issueTokens(1004L, "operator");
    tokenService.revokeAccessToken(tokenPair.accessToken());

    AppException exception =
        assertThrows(AppException.class, () -> tokenService.parseAccessToken(tokenPair.accessToken()));
    assertEquals(ErrorCodes.AUTH_UNAUTHORIZED, exception.getCode());
  }

  @Test
  void parseAccessTokenShouldFailWhenTokenExpired() {
    MutableClock clock = new MutableClock(Instant.parse("2026-02-26T10:00:00Z"));
    JwtTokenService tokenService =
        new JwtTokenService(
            clock,
            new ObjectMapper(),
            "labor-system-jwt-test-secret",
            Duration.ofSeconds(10),
            Duration.ofDays(7));

    TokenPair tokenPair = tokenService.issueTokens(1005L, "viewer");
    clock.plusSeconds(11);

    AppException exception =
        assertThrows(AppException.class, () -> tokenService.parseAccessToken(tokenPair.accessToken()));
    assertEquals(ErrorCodes.AUTH_UNAUTHORIZED, exception.getCode());
    assertTrue(exception.getMessage().contains("令牌"));
  }

  private static final class MutableClock extends Clock {

    private Instant instant;
    private final ZoneId zoneId;

    private MutableClock(Instant instant) {
      this(instant, ZoneId.of("UTC"));
    }

    private MutableClock(Instant instant, ZoneId zoneId) {
      this.instant = instant;
      this.zoneId = zoneId;
    }

    @Override
    public ZoneId getZone() {
      return zoneId;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return new MutableClock(instant, zone);
    }

    @Override
    public Instant instant() {
      return instant;
    }

    private void plusSeconds(long seconds) {
      this.instant = this.instant.plusSeconds(seconds);
    }
  }
}

