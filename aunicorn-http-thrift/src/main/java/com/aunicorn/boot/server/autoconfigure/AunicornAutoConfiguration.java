package com.aunicorn.boot.server.autoconfigure;

import com.aunicorn.boot.server.advice.AunicornAdviceDiscoverer;
import com.aunicorn.boot.server.service.AunicornService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ConditionalOnClass(AunicornService.class)
@ConditionalOnWebApplication
public class AunicornAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	@Lazy
    AunicornAdviceDiscoverer aunicornAdviceDiscoverer(final ApplicationContext applicationContext) {
		return new AunicornAdviceDiscoverer(applicationContext);
	}
}
