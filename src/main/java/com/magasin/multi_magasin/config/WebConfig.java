package com.magasin.multi_magasin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/multi_magasin", c ->
                c.getPackageName().startsWith("com.magasin.multi_magasin")
                        && !c.getName().equals("com.magasin.multi_magasin.RootRedirectController")
        );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadDir.toUri().toString());
        registry.addResourceHandler("/multi_magasin/uploads/**")
                .addResourceLocations(uploadDir.toUri().toString());
    }
}
