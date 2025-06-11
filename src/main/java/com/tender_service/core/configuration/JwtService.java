package com.tender_service.core.configuration;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.tender_service.core.api.database.entity.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.refresh.secret}")
    private String REFRESH_SECRET;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 часа
    private static final long REFRESH_EXPIRATION_TIME = 1000L * 60 * 60 * 24 * 30; // 30 дней

    public String generateToken(String email, String username, Set<Role> roles) {
        try {
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(email)
                    .claim("username", username)
                    .claim("roles", roles)
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .build();

            JWSSigner signer = new MACSigner(SECRET.getBytes());

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claimsSet
            );

            signedJWT.sign(signer);

            return signedJWT.serialize();

        } catch (Exception e) {
            throw new RuntimeException("Error during generation", e);
        }
    }

    public String generateRefreshToken(String email) {
        try {
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(email)
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME))
                    .build();

            JWSSigner signer = new MACSigner(REFRESH_SECRET.getBytes());

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claimsSet
            );

            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Error during generation", e);
        }
    }

    public String getEmailFromToken(String token) throws ParseException {
        return parseToken(token).getJWTClaimsSet().getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            SignedJWT jwt = parseToken(token);
            JWSVerifier verifier = new MACVerifier(SECRET.getBytes());
            return jwt.verify(verifier) && !isExpired(jwt);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpiredAndValid(String token){
        try {
            SignedJWT jwt = parseToken(token);
            JWSVerifier verifier = new MACVerifier(SECRET.getBytes());
            return jwt.verify(verifier) && isExpired(jwt);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshTokenValid(String refreshToken) {
        try {
            SignedJWT jwt = parseToken(refreshToken);
            JWSVerifier verifier = new MACVerifier(REFRESH_SECRET.getBytes());
            return jwt.verify(verifier) && !isExpired(jwt);
        } catch (Exception e) {
            return false;
        }
    }

    private SignedJWT parseToken(String token) throws ParseException {
        return SignedJWT.parse(token);
    }

    private boolean isExpired(SignedJWT jwt) throws ParseException {
        Date expiration = jwt.getJWTClaimsSet().getExpirationTime();
        return expiration.before(new Date());
    }
}
