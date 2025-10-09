package org.example.brainbuster.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // İzin verilen origin'ler - localhost:5173 (Vite) ve 3000 (React)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:3000",
            "http://127.0.0.1:5173"
        ));
        
        // İzin verilen HTTP metotları
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // İzin verilen header'lar
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Authorization header'ı gibi credential'ları göndermeye izin ver
        configuration.setAllowCredentials(true);
        
        // Preflight request cache süresi (1 saat)
        configuration.setMaxAge(3600L);
        
        // Exposed header'lar (frontend'in okuyabileceği custom header'lar)
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        // Tüm path'lere bu yapılandırmayı uygula
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}