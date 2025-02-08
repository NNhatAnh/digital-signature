package com.example.digital_signature.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.*;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final String secretKey = "NanhHungry64ByteLongSecureSecretKey";

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    public String generateToken(Object data) {
        return Jwts.builder()
                .claim("data", data) 
                .setIssuedAt(new Date()) 
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 20))  
                .serializeToJsonWith(new JacksonSerializer<>(getObjectMapper())) 
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) 
                .compact();
    }
}

