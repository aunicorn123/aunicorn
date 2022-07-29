package com.aunicorn.boot.client.autoconfigure;

import com.aunicorn.boot.client.inject.AunicornClientBeanPostProcessor;
import com.aunicorn.boot.client.inject.THttpClientFactory;
import com.aunicorn.boot.client.stubfactory.SyncStubFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class AunicornClientAutoConfiguration {

    @Bean
    @Lazy
    public AunicornClientBeanPostProcessor aunicornClientBeanPostProcessor(final ApplicationContext applicationContext) {
        return new AunicornClientBeanPostProcessor(applicationContext);
    }

    @Bean
    @Lazy
    TBinaryProtocol.Factory tBinaryProtocolFactory() {
        return new TBinaryProtocol.Factory();
    }

    @Bean
    @Lazy
    THttpClientFactory tHttpClientFactory() {
        return new THttpClientFactory();
    }

    @Bean
    SyncStubFactory syncStubFactory() {
        return new SyncStubFactory();
    }
}
