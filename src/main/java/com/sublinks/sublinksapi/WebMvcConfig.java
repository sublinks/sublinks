package com.sublinks.sublinksapi;

import com.sublinks.sublinksapi.interceptors.RateLimiter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
  @Override
  public void addInterceptors(InterceptorRegistry registry){
    registry.addInterceptor(new RateLimiter())
        .excludePathPatterns("/api/v3/site");
  }
}
