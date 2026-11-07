package com.ludus.api;

import com.ludus.api.security.JwtFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class LudusApiApplication {

    public static void main(String[] args) {
        cargarVariablesEnv();
        SpringApplication.run(LudusApiApplication.class, args);
    }

    /**
     * Carga variables de entorno desde un archivo .env local si existe,
     * protegiendo credenciales sensibles para que no se expongan en el código.
     */
    private static void cargarVariablesEnv() {
        Path envFile = Path.of(".env");
        if (Files.exists(envFile)) {
            try {
                for (String line : Files.readAllLines(envFile)) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                        int sep = line.indexOf('=');
                        String key = line.substring(0, sep).trim();
                        String value = line.substring(sep + 1).trim();
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterFilter(){
        FilterRegistrationBean<JwtFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new JwtFilter());

        // Aplicamos el filtro a TODAS las rutas de Ludus
        registrationBean.addUrlPatterns("/ludus/*");

        return registrationBean;
    }
}
