package cn.aunicorn.boot.client.inject;

import cn.aunicorn.boot.client.stubfactory.StubFactory;
import cn.aunicorn.boot.common.AunicornRpcContext;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;

public class AunicornClientBeanPostProcessor  implements BeanPostProcessor {

    private final ApplicationContext applicationContext;
    private TBinaryProtocol.Factory tBinaryProtocolFactory = null;
    private AunicornTTransportFactory tHttpClientFactory = null;
    private List<StubFactory> stubFactories = null;

    /**
     * Creates a newAunicornClientBeanPostProcessor with the given ApplicationContext.
     *
     */
    public AunicornClientBeanPostProcessor(final ApplicationContext applicationContext) {
        this.applicationContext = requireNonNull(applicationContext, "applicationContext");
    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        for(Class<?> clazz = bean.getClass(); clazz != null; clazz = clazz.getSuperclass()){
            processFields(clazz, bean);
        }
        return bean;
    }

    private void processFields(final Class<?> clazz, final Object bean) {
        for (final Field field : clazz.getDeclaredFields()) {
            final AunicornClient annotation = AnnotationUtils.findAnnotation(field, AunicornClient.class);
            if (annotation == null) {
                continue;
            }
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, bean, processInjectionPoint(field, field.getType(), annotation));
        }
    }

    protected <T> T processInjectionPoint(final Member injectionTarget, final Class<T> injectionType, final AunicornClient annotation) {

        final AunicornRpcContext rpcContext = new AunicornRpcContext();
        rpcContext.setServiceId(annotation.serviceId());
        rpcContext.setUrl(annotation.url());

        TProtocol protocol;
        String injectionTypeName = injectionType.getDeclaringClass().getName();
        try {
            TTransport transport = getTHttpClientFactory().getTransport(rpcContext);
            protocol  = getTBinaryProtocolFactory().getProtocol(transport);
            if (protocol == null) {
                throw new IllegalStateException("protocol factory created a null protocol for " + injectionTypeName);
            }
        } catch (final RuntimeException e) {
            throw new IllegalStateException("Failed to create protocol: " + injectionTypeName, e);
        }

        final T value = valueForMember(injectionType, protocol);
        if (value == null) {
            throw new IllegalStateException("Injection value is null unexpectedly for " + injectionTypeName + " at " + injectionTarget);
        }
        return value;
    }

    protected <T> T valueForMember(final Class<T> injectionType, final TProtocol protocol) {
        try{
            final StubFactory factory = getStubFactories().stream().filter(stubFactory -> stubFactory.isApplicable( injectionType))
                    .findFirst().orElseThrow(() -> new BeanInstantiationException(injectionType, "Unsupported stub type: " + injectionType.getName() + " -> Please report this issue."));
            Object stub =  factory.createStub(injectionType, protocol);
            return injectionType.cast(stub);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Lazy getter for the {@link TBinaryProtocol.Factory}.
     *
     * @return The TBinaryProtocol factory to use.
     */
    private TBinaryProtocol.Factory getTBinaryProtocolFactory(){
        if (this.tBinaryProtocolFactory == null) {
            final TBinaryProtocol.Factory factory = this.applicationContext.getBean(TBinaryProtocol.Factory.class);
            this.tBinaryProtocolFactory = factory;
            return factory;
        }
        return this.tBinaryProtocolFactory;
    }

    /**
     * Lazy getter for the {@link AunicornTTransportFactory}.
     *
     * @return The THttpClient  factory to use.
     */
    private AunicornTTransportFactory getTHttpClientFactory(){
        if (this.tHttpClientFactory == null) {
            final AunicornTTransportFactory factory = this.applicationContext.getBean(AunicornTTransportFactory.class);
            this.tHttpClientFactory = factory;
            return factory;
        }
        return this.tHttpClientFactory;
    }

    /**
     * Lazy getter for the list of defined {@link StubFactory} beans.
     *
     * @return A list of all defined {@link StubFactory} beans.
     */
    private List<StubFactory> getStubFactories() {
        if (this.stubFactories == null) {
            this.stubFactories = new ArrayList<StubFactory>(this.applicationContext.getBeansOfType(StubFactory.class).values());
        }
        return this.stubFactories;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
