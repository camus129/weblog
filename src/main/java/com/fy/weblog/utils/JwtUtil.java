package com.fy.weblog.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT工具类（适配JJWT 0.12.3版本）
 */
public class JwtUtil {

    /**
     * 签名密钥
     * 注意：生产环境建议从配置文件或KMS中获取，不要硬编码
     * 密钥至少32字节（256位）以支持HS256算法
     */
    private static final String SECRET = "2WE2MzpCdnGJFxAl0aZXq3ztrmGztNv0Bk1vIGFP154=";

    /**
     * 将密钥转换为SecretKey对象
     */
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /**
     * Access Token过期时间（毫秒）
     * 默认15分钟，用于API访问验证
     */
    private static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000;

    /**
     * Refresh Token过期时间（毫秒）
     * 默认7天，用于刷新Access Token
     */
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000;

    /**
     * 签发者
     * 标识Token的签发系统，建议从配置文件读取
     */
    private static final String ISSUER = "system";

    /**
     * 生成基础的JWT令牌
     * 包含标准声明：sub（主题）、iss（签发者）、iat（签发时间）、exp（过期时间）、jti（唯一标识）
     *
     * @param subject 令牌主题，通常是用户ID或用户名，必填
     * @return 生成的JWT令牌字符串
     */
    public static String generateToken(String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);

        return Jwts.builder()
                .subject(subject)                                    // 设置主题（用户标识）
                .issuer(ISSUER)                                      // 设置签发者
                .issuedAt(now)                                       // 设置签发时间
                .expiration(exp)                                     // 设置过期时间
                .id(UUID.randomUUID().toString())                    // 设置JWT ID，用于防重放攻击
                .signWith(SECRET_KEY, Jwts.SIG.HS256)                // 使用HS256算法签名
                .compact();                                          // 生成紧凑的JWT字符串
    }

    /**
     * 生成包含自定义声明的JWT令牌
     *
     * @param subject 令牌主题，通常是用户ID或用户名，必填
     * @param claims 自定义声明数据，可包含用户角色、权限等信息，可为null
     * @return 生成的JWT令牌字符串
     */
    public static String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);

        JwtBuilder builder = Jwts.builder()
                .subject(subject)
                .issuer(ISSUER)
                .issuedAt(now)
                .expiration(exp)
                .id(UUID.randomUUID().toString())
                .signWith(SECRET_KEY, Jwts.SIG.HS256);

        // 如果有自定义声明，添加到JWT中
        if (claims != null && !claims.isEmpty()) {
            builder.claims(claims);
        }

        return builder.compact();
    }

    /**
     * 生成Access Token
     * Access Token用于API访问验证，有效期较短（默认15分钟）
     *
     * @param subject 令牌主题，通常是用户ID或用户名
     * @param claims 自定义声明数据，如用户角色、权限等
     * @return 生成的Access Token字符串
     */
    public static String generateAccessToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);

        JwtBuilder builder = Jwts.builder()
                .subject(subject)
                .issuer(ISSUER)
                .issuedAt(now)
                .expiration(exp)
                .id(UUID.randomUUID().toString())
                .signWith(SECRET_KEY, Jwts.SIG.HS256);

        if (claims != null && !claims.isEmpty()) {
            builder.claims(claims);
        }

        return builder.compact();
    }

    /**
     * 生成Refresh Token
     * Refresh Token用于刷新Access Token，有效期较长（默认7天）
     * 建议将Refresh Token存储在数据库或Redis中
     *
     * @param subject 令牌主题，通常是用户ID或用户名
     * @return 生成的Refresh Token字符串
     */
    public static String generateRefreshToken(String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION);

        return Jwts.builder()
                .subject(subject)
                .issuer(ISSUER + "-refresh")                         // Refresh Token的签发者标识
                .issuedAt(now)
                .expiration(exp)
                .id(UUID.randomUUID().toString())
                .signWith(SECRET_KEY, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析并验证JWT令牌
     * 验证内容包括：签名、过期时间、格式等
     *
     * @param token 待解析的JWT令牌字符串
     * @return 解析出的Claims对象，包含所有声明
     * @throws JwtException 令牌无效
     */
    public static Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)                              // 设置验证密钥
                .build()
                .parseSignedClaims(token)                            // 解析JWT
                .getPayload();                                       // 获取载荷（Claims）
    }

    /**
     * 验证令牌的有效性
     * 不返回详细信息，只验证令牌是否有效
     *
     * @param token 待验证的JWT令牌字符串
     * @return true表示令牌有效，false表示令牌无效
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 检查令牌是否过期
     *
     * @param token 待检查的JWT令牌字符串
     * @return true表示令牌已过期，false表示令牌未过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;                                            // 捕获过期异常，直接返回true
        } catch (JwtException e) {
            return true;                                            // 其他异常也视为过期
        }
    }

    /**
     * 从令牌中提取主题（通常是用户ID）
     *
     * @param token JWT令牌字符串
     * @return 令牌的主题
     */
    public static String getSubjectFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从令牌中获取指定的自定义声明
     *
     * @param token JWT令牌字符串
     * @param claimName 声明的名称
     * @return 声明的值（转换为String类型）
     */
    public static String getClaimFromToken(String token, String claimName) {
        Claims claims = parseToken(token);
        return claims.get(claimName, String.class);
    }

    /**
     * 从令牌中获取指定类型的自定义声明
     *
     * @param token JWT令牌字符串
     * @param claimName 声明的名称
     * @param clazz 期望的类型
     * @param <T> 泛型类型
     * @return 声明的值
     */
    public static <T> T getClaimFromToken(String token, String claimName, Class<T> clazz) {
        Claims claims = parseToken(token);
        return claims.get(claimName, clazz);
    }

    /**
     * 从令牌中获取过期时间
     *
     * @param token JWT令牌字符串
     * @return 令牌的过期时间
     */
    public static Date getExpirationDateFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    /**
     * 使用Refresh Token刷新Access Token
     *
     * @param refreshToken Refresh Token字符串
     * @return 新的Access Token字符串
     * @throws JwtException Refresh Token无效或过期
     */
    public static String refreshAccessToken(String refreshToken) throws JwtException {
        // 解析Refresh Token
        Claims claims = parseToken(refreshToken);

        // 提取用户标识
        String subject = claims.getSubject();

        // 获取Refresh Token中的自定义声明（如果有）
        Map<String, Object> newClaims = new HashMap<>();
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            if (!entry.getKey().equals("iss")) {                     // 排除签发者
                newClaims.put(entry.getKey(), entry.getValue());
            }
        }

        // 生成新的Access Token
        return generateAccessToken(subject, newClaims);
    }

    /**
     * 计算Token的剩余有效时间（秒）
     *
     * @param token JWT令牌字符串
     * @return 剩余有效时间（秒），如果Token已过期返回0
     */
    public static long getRemainingTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            Date now = new Date();
            long remaining = (expiration.getTime() - now.getTime()) / 1000;
            return remaining > 0 ? remaining : 0;
        } catch (JwtException e) {
            return 0;
        }
    }

    /**
     * 检查Token是否即将过期
     *
     * @param token JWT令牌字符串
     * @param thresholdSeconds 阈值（秒），在此时间范围内视为即将过期
     * @return true表示Token即将过期，false表示Token还有足够长的有效期
     */
    public static boolean isTokenExpiringSoon(String token, long thresholdSeconds) {
        long remainingTime = getRemainingTime(token);
        return remainingTime > 0 && remainingTime <= thresholdSeconds;
    }

    /**
     * 验证Token是否符合指定的签发者
     *
     * @param token JWT令牌字符串
     * @param expectedIssuer 期望的签发者
     * @return true表示签发者匹配，false表示不匹配
     */
    public static boolean verifyIssuer(String token, String expectedIssuer) {
        try {
            Claims claims = parseToken(token);
            String issuer = claims.getIssuer();
            return expectedIssuer.equals(issuer);
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 生成一个长期有效的Token
     * 用于某些特殊场景，如激活链接、密码重置等
     *
     * @param subject 令牌主题
     * @param expirationHours 有效期（小时）
     * @return 生成的Token字符串
     */
    public static String generateLongLivedToken(String subject, long expirationHours) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationHours * 60 * 60 * 1000);

        return Jwts.builder()
                .subject(subject)
                .issuer(ISSUER)
                .issuedAt(now)
                .expiration(exp)
                .id(UUID.randomUUID().toString())
                .signWith(SECRET_KEY, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 生成安全的随机密钥
     * 可用于生成新的密钥对
     *
     * @return Base64编码的密钥
     */
    public static String generateSecretKey() {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        return Encoders.BASE64.encode(key.getEncoded());
    }

    /**
     * 从Base64字符串创建密钥
     *
     * @param base64Key Base64编码的密钥
     * @return SecretKey对象
     */
    public static SecretKey createSecretKeyFromBase64(String base64Key) {
        byte[] keyBytes = Decoders.BASE64.decode(base64Key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 测试方法
     */
    public static void main(String[] args) {
        try {
            System.out.println("=== JWT工具类测试（JJWT 0.12.3） ===\n");

            // 1. 生成一个简单的Token
            String token = generateToken("user123");
            System.out.println("1. 生成的Token: " + token);
            System.out.println("   Token长度: " + token.length());

            // 2. 解析Token
            Claims claims = parseToken(token);
            System.out.println("\n2. 解析Token:");
            System.out.println("   Subject: " + claims.getSubject());
            System.out.println("   Issuer: " + claims.getIssuer());
            System.out.println("   IssuedAt: " + claims.getIssuedAt());
            System.out.println("   Expiration: " + claims.getExpiration());
            System.out.println("   JWT ID: " + claims.getId());

            // 3. 生成包含自定义声明的Token
            Map<String, Object> customClaims = new HashMap<>();
            customClaims.put("username", "张三");
            customClaims.put("role", "admin");
            customClaims.put("department", "技术部");

            String tokenWithClaims = generateToken("user123", customClaims);
            System.out.println("\n3. 生成的Token（带自定义声明）: " + tokenWithClaims);

            // 4. 解析自定义声明
            Claims claimsWithCustom = parseToken(tokenWithClaims);
            System.out.println("\n4. 解析自定义声明:");
            System.out.println("   Username: " + claimsWithCustom.get("username"));
            System.out.println("   Role: " + claimsWithCustom.get("role"));
            System.out.println("   Department: " + claimsWithCustom.get("department"));

            // 5. 验证Token
            boolean isValid = validateToken(token);
            System.out.println("\n5. Token是否有效: " + isValid);

            // 6. 检查Token是否过期
            boolean isExpired = isTokenExpired(token);
            System.out.println("6. Token是否过期: " + isExpired);

            // 7. 获取剩余有效时间
            long remaining = getRemainingTime(token);
            System.out.println("7. Token剩余有效时间: " + remaining + "秒");

            // 8. 生成Access Token和Refresh Token
            Map<String, Object> accessClaims = new HashMap<>();
            accessClaims.put("role", "admin");

            String accessToken = generateAccessToken("user123", accessClaims);
            String refreshToken = generateRefreshToken("user123");

            System.out.println("\n8. Access Token: " + accessToken.substring(0, 50) + "...");
            System.out.println("   Refresh Token: " + refreshToken.substring(0, 50) + "...");

            // 9. 刷新Access Token
            String newAccessToken = refreshAccessToken(refreshToken);
            System.out.println("\n9. 刷新后的Access Token: " + newAccessToken.substring(0, 50) + "...");

            // 10. 检查Token即将过期
            boolean expiringSoon = isTokenExpiringSoon(token, 600);  // 10分钟阈值
            System.out.println("10. Token是否即将过期（10分钟内）: " + expiringSoon);

            // 11. 验证签发者
            boolean issuerMatch = verifyIssuer(token, ISSUER);
            System.out.println("11. 签发者是否匹配: " + issuerMatch);

            // 12. 生成长期有效的Token（24小时）
            String longLivedToken = generateLongLivedToken("user456", 24);
            System.out.println("\n12. 长期有效Token（24小时）: " + longLivedToken.substring(0, 50) + "...");

            // 13. 生成新的密钥
            String newSecretKey = generateSecretKey();
            System.out.println("\n13. 生成的新密钥（Base64）: " + newSecretKey);

        } catch (Exception e) {
            System.err.println("测试过程中发生错误:");
            e.printStackTrace();
        }
    }
}
