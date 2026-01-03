package org.example.hotel.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

    private static final String SECRET = "very-secret-key-for-demo-only-change-me";

    @Bean
    public SecretKey jwtSecretKeyHotel() {
        return new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKeyHotel) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKeyHotel).build();
    }
}
