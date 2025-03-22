package com.datepoker.dp_backend.util;

import com.datepoker.dp_backend.argument_resolvers.CurrentUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    public WebMvcConfig(CurrentUserArgumentResolver resolver) {
        this.currentUserArgumentResolver = resolver;
    }

    public void addArgumentResolvers(List<org.springframework.web.method.support.HandlerMethodArgumentResolver> resolvers) {
        resolvers.add((HandlerMethodArgumentResolver) currentUserArgumentResolver);
    }
}

