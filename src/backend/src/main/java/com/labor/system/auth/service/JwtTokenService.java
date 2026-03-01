package com.labor.system.auth.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.labor.system.auth.model.TokenPair;
import com.labor.system.auth.model.TokenPrincipal;
import com.labor.system.common.api.AppException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

  private static final String TOKEN_TYPE_ACCESS = "access";
  private static final String TOKEN_TYPE_REFRESH = "refresh";
  private static final String HMAC_ALGORITHM = "HmacSHA256";

  private final Clock clock;
  private final ObjectMapper objectMapper;
  private final byte[] secretKey;
  private final Duration accessTokenTtl;
  private final Duration refreshTokenTtl;
  private final ConcurrentMap<String, RefreshTokenState> refreshTokenStore = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Boolean> revokedAccessTokenStore = new ConcurrentHashMap<>();

  public JwtTokenService(
      ObjectMapper objectMapper,
      @Value("${app.auth.jwt.secret:labor-system-dev-jwt-secret-please-change-this-value}") String secret,
      @Value("${app.auth.jwt.access-ttl-seconds:3600}") long accessTokenTtlSeconds,
      @Value("${app.auth.jwt.refresh-ttl-seconds:604800}") long refreshTokenTtlSeconds) {
    this(
        Clock.systemUTC(),
        objectMapper,
        secret,
        Duration.ofSeconds(accessTokenTtlSeconds),
        Duration.ofSeconds(refreshTokenTtlSeconds));
  }

  JwtTokenService(
      Clock clock,
      ObjectMapper objectMapper,
      String secret,
      Duration accessTokenTtl,
      Duration refreshTokenTtl) {
    this.clock = clock;
    this.objectMapper = objectMapper;
    this.secretKey = secret.getBytes(StandardCharsets.UTF_8);
    this.accessTokenTtl = accessTokenTtl;
    this.refreshTokenTtl = refreshTokenTtl;
  }

  public TokenPair issueTokens(Long userId, String username) {
    purgeExpiredTokens();
    String accessToken = createToken(userId, username, TOKEN_TYPE_ACCESS, accessTokenTtl);
    String refreshToken = createToken(userId, username, TOKEN_TYPE_REFRESH, refreshTokenTtl);
    long refreshExp = clock.instant().plus(refreshTokenTtl).getEpochSecond();
    refreshTokenStore.put(refreshToken, new RefreshTokenState(userId, username, refreshExp));
    return new TokenPair(accessToken, refreshToken, accessTokenTtl.toSeconds());
  }

  public TokenPair refreshAccessToken(String refreshToken) {
    purgeExpiredTokens();
    ParsedToken parsedRefreshToken = parseToken(refreshToken, TOKEN_TYPE_REFRESH);
    RefreshTokenState refreshTokenState = refreshTokenStore.get(refreshToken);
    if (refreshTokenState == null || refreshTokenState.expEpochSeconds() <= clock.instant().getEpochSecond()) {
      refreshTokenStore.remove(refreshToken);
      throw AppException.unauthorized("未认证或令牌失效");
    }
    if (!refreshTokenState.userId().equals(parsedRefreshToken.userId())
        || !refreshTokenState.username().equals(parsedRefreshToken.username())) {
      refreshTokenStore.remove(refreshToken);
      throw AppException.unauthorized("未认证或令牌失效");
    }
    String newAccessToken =
        createToken(parsedRefreshToken.userId(), parsedRefreshToken.username(), TOKEN_TYPE_ACCESS, accessTokenTtl);
    return new TokenPair(newAccessToken, refreshToken, accessTokenTtl.toSeconds());
  }

  public TokenPrincipal parseAccessToken(String accessToken) {
    if (revokedAccessTokenStore.containsKey(accessToken)) {
      throw AppException.unauthorized("未认证或令牌失效");
    }
    ParsedToken parsedToken = parseToken(accessToken, TOKEN_TYPE_ACCESS);
    return new TokenPrincipal(parsedToken.userId(), parsedToken.username());
  }

  public void invalidateRefreshToken(String refreshToken) {
    refreshTokenStore.remove(refreshToken);
  }

  public void revokeAccessToken(String accessToken) {
    revokedAccessTokenStore.put(accessToken, Boolean.TRUE);
  }

  private void purgeExpiredTokens() {
    long now = clock.instant().getEpochSecond();
    refreshTokenStore.entrySet().removeIf(entry -> entry.getValue().expEpochSeconds() <= now);
  }

  private String createToken(Long userId, String username, String tokenType, Duration ttl) {
    Instant now = clock.instant();
    Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("uid", userId);
    payload.put("sub", username);
    payload.put("type", tokenType);
    payload.put("jti", UUID.randomUUID().toString());
    payload.put("iat", now.getEpochSecond());
    payload.put("exp", now.plus(ttl).getEpochSecond());
    String encodedHeader = base64UrlEncode(toJsonBytes(header));
    String encodedPayload = base64UrlEncode(toJsonBytes(payload));
    String signingInput = encodedHeader + "." + encodedPayload;
    String signature = sign(signingInput);
    return signingInput + "." + signature;
  }

  private ParsedToken parseToken(String token, String expectedType) {
    try {
      String[] segments = token.split("\\.");
      if (segments.length != 3) {
        throw AppException.unauthorized("未认证或令牌失效");
      }
      String signingInput = segments[0] + "." + segments[1];
      String expectedSignature = sign(signingInput);
      if (!MessageDigest.isEqual(
          expectedSignature.getBytes(StandardCharsets.US_ASCII),
          segments[2].getBytes(StandardCharsets.US_ASCII))) {
        throw AppException.unauthorized("未认证或令牌失效");
      }

      byte[] payloadJson = base64UrlDecode(segments[1]);
      Map<String, Object> claims = objectMapper.readValue(payloadJson, new TypeReference<>() {});
      String tokenType = asString(claims.get("type"));
      if (!expectedType.equals(tokenType)) {
        throw AppException.unauthorized("未认证或令牌失效");
      }

      long exp = asLong(claims.get("exp"));
      if (exp <= clock.instant().getEpochSecond()) {
        throw AppException.unauthorized("未认证或令牌失效");
      }

      return new ParsedToken(asLong(claims.get("uid")), asString(claims.get("sub")), exp);
    } catch (AppException ex) {
      throw ex;
    } catch (Exception ex) {
      throw AppException.unauthorized("未认证或令牌失效");
    }
  }

  private byte[] toJsonBytes(Map<String, Object> data) {
    try {
      return objectMapper.writeValueAsBytes(data);
    } catch (Exception ex) {
      throw new IllegalStateException("Cannot serialize JWT payload", ex);
    }
  }

  private String sign(String signingInput) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(new SecretKeySpec(secretKey, HMAC_ALGORITHM));
      return base64UrlEncode(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception ex) {
      throw new IllegalStateException("Cannot sign JWT token", ex);
    }
  }

  private String base64UrlEncode(byte[] bytes) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private byte[] base64UrlDecode(String value) {
    return Base64.getUrlDecoder().decode(value);
  }

  private String asString(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private long asLong(Object value) {
    if (value instanceof Number number) {
      return number.longValue();
    }
    return Long.parseLong(String.valueOf(value));
  }

  private record ParsedToken(Long userId, String username, long expEpochSeconds) {}

  private record RefreshTokenState(Long userId, String username, long expEpochSeconds) {}
}

