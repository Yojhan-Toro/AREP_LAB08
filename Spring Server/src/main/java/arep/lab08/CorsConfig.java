package arep.lab08;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(
                            // Local
                            "http://localhost:35000",
                            "https://localhost:35000",
                            // AWS — Máquina 2 Apache
                            "http://ec2-44-208-167-208.compute-1.amazonaws.com:35000",
                            "https://ec2-44-208-167-208.compute-1.amazonaws.com:35000",
                            // AWS — Máquina 1 Proxy (el browser ve esta URL)
                            "http://ec2-100-55-41-160.compute-1.amazonaws.com",
                            "https://ec2-100-55-41-160.compute-1.amazonaws.com"
                        )
                        .allowedMethods("GET", "POST", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization");
            }
        };
    }
}
