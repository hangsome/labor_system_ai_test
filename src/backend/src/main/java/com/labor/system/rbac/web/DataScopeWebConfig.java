package com.labor.system.rbac.web;

import com.labor.system.auth.service.JwtTokenService;
import com.labor.system.rbac.service.DataScopeService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DataScopeWebConfig implements WebMvcConfigurer {

  private final JwtTokenService jwtTokenService;
  private final DataScopeService dataScopeService;

  public DataScopeWebConfig(
      ObjectProvider<JwtTokenService> jwtTokenServiceProvider,
      ObjectProvider<DataScopeService> dataScopeServiceProvider) {
    this.jwtTokenService = jwtTokenServiceProvider.getIfAvailable();
    this.dataScopeService = dataScopeServiceProvider.getIfAvailable();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(new DataScopeInterceptor(jwtTokenService, dataScopeService))
        .addPathPatterns("/api/admin/v1/system/**")
        .excludePathPatterns("/error");
  }
}
