package vn.iotstar.jobhub_hcmute_be.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.iotstar.jobhub_hcmute_be.repository.UserRepository;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
@Log4j2
public class JwtTokenProvider {

    @Autowired
    UserRepository userRepository;

    private final Long JWT_ACCESS_EXPIRATION = 24 * 60 * 60 * 1000L;
    private final Long JWT_REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;
    // Thời gian hết hạn cho mã thông báo làm mới: 1 tuần (7 ngày)

    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private final String issuer = "Nhom 8 TLCN";

    private Key getSigningKey() {
        return secretKey;
    }

    public String generateAccessToken(UserDetail userDetail) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_ACCESS_EXPIRATION);

        System.out.println(secretKey.getEncoded());

        return Jwts.builder()
                .setSubject((userDetail.getUser().getUserId()))
                .claim("userId", userDetail.getUser().getUserId())
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

    }

    public String generateRefreshToken(UserDetail userDetail) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_REFRESH_EXPIRATION);

        return Jwts.builder()
                .setSubject((userDetail.getUser().getUserId()))
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

    }


    public String getUserIdFromJwt(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return ((String) claims.get("userId"));
    }

    public String getUserIdFromRefreshToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return (claims.getSubject());
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey().getEncoded()).build().parseClaimsJws(authToken);
            return true;
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }

}
