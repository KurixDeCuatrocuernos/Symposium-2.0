package com.sympos2.securities;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * This class contains Beans to configure the Web connection.
 * @author KurixDeCuatroCuernos
 * @version 0.1.0
 * @see WebMvcConfigurer
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	/**
	 * This function configures the Web connection to allows CORS registry, in order to allow connection with React's front.
	 * @param registry CorsRegitry to set the specification of CORS. 
	 */
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

