package com.casacrew.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String uploadDir;

    public WebConfig(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    // Deze handler wijst UITSLUITEND naar de "public"-submap, niet naar de
    // hele upload-root. Profielfoto's en taakfoto's horen hier thuis (ze
    // worden rechtstreeks als <img src> geladen, zonder auth-header).
    // Contracten en documenten staan in een "private"-submap die deze
    // handler bewust niet kent -- die zijn alleen bereikbaar via de
    // geauthenticeerde download-endpoints, die rechtstreeks van schijf
    // lezen en dus niet van deze static-resource-mapping afhangen.
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadDir, "public").toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("classpath:/static/uploads/", absolutePath + "/");
    }
}