package com.sympos2.securities;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Permite solicitudes desde el puerto 5173 (frontend React)
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")  // Dirección del frontend (React)
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // Métodos HTTP permitidos
                .allowedHeaders("*")  // Permite cualquier encabezado
        		.allowCredentials(true);
    }
}

