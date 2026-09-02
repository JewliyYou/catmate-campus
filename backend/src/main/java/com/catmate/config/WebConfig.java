package com.catmate.config;

import com.catmate.auth.AuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;
    private final String corsOrigin;
    public WebConfig(AuthInterceptor authInterceptor, @Value("${catmate.cors-origin:http://localhost:5173}") String corsOrigin) { this.authInterceptor = authInterceptor; this.corsOrigin = corsOrigin; }
    @Override public void addInterceptors(InterceptorRegistry registry) { registry.addInterceptor(authInterceptor).addPathPatterns("/api/**").excludePathPatterns("/api/auth/login", "/api/auth/register"); }
    @Override public void addCorsMappings(CorsRegistry registry) { registry.addMapping("/api/**").allowedOriginPatterns(corsOrigin,"http://127.0.0.1:5173").allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS").allowedHeaders("*").allowCredentials(true); }
}
