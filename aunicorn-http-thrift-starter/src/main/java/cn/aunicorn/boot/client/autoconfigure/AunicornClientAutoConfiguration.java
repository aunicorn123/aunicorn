package cn.aunicorn.boot.client.autoconfigure;

import cn.aunicorn.boot.client.inject.AunicornClientBeanPostProcessor;
import cn.aunicorn.boot.client.inject.AunicornTTransportFactory;
import cn.aunicorn.boot.client.stubfactory.AsyncStubFactory;
import cn.aunicorn.boot.client.stubfactory.SyncStubFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TTransportFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@AutoConfigureAfter(name = "org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration")
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
    AunicornTTransportFactory tHttpClientFactory(final ApplicationContext applicationContext) {
        return new AunicornTTransportFactory(applicationContext);
    }

    @Bean
    @Lazy
    TTransportFactory tTransportFactory() {
        return new TTransportFactory();
    }

    @Bean
    SyncStubFactory syncStubFactory() {
        return new SyncStubFactory();
    }

//    @Bean
    AsyncStubFactory asyncStubFactory() {
        return new AsyncStubFactory();
    }
}
