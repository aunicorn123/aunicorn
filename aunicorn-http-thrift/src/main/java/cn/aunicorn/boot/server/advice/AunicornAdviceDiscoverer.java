package cn.aunicorn.boot.server.advice;

import cn.aunicorn.boot.server.service.AunicornService;
import cn.aunicorn.boot.server.servlet.AunicornServlet;
import org.apache.thrift.TProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import static java.util.Objects.requireNonNull;

@Configuration
public class AunicornAdviceDiscoverer implements ServletContextInitializer {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String TYPE_THRIFT = "thrift" ;
    private final ApplicationContext applicationContext;

    public AunicornAdviceDiscoverer(final ApplicationContext applicationContext) {
        this.applicationContext = requireNonNull(applicationContext, "applicationContext");
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        Arrays.asList(applicationContext.getBeanNamesForAnnotation(AunicornService.class)).stream().forEach(beanName->{
            AunicornService annotation = applicationContext.findAnnotationOnBean(beanName, AunicornService.class);
            try {
                discovererThriftServlet(servletContext, annotation.value(), applicationContext.getBean(beanName));
            } catch ( NoSuchMethodException e) {
                logger.error("onStartup error", e);
            }
        });
    }

    protected void discovererThriftServlet(ServletContext servletContext, String url, Object handler) throws NoSuchMethodException {

        Class<?>[] handlerInterfaces = AopUtils.getTargetClass(handler).getInterfaces();
        Class<?> ifaceClass = null;
        Class<TProcessor> processorClass = null;
        Class<?> serviceClass = null;

        for (Class<?> handlerInterfaceClass : handlerInterfaces) {
            if (!handlerInterfaceClass.getName().endsWith("$Iface")) {
                continue;
            }
            serviceClass = handlerInterfaceClass.getDeclaringClass();
            if (serviceClass == null) {
                continue;
            }
            for (Class<?> innerClass : serviceClass.getDeclaredClasses()) {
                if (!innerClass.getName().endsWith("$Processor")) {
                    continue;
                }
                if (!TProcessor.class.isAssignableFrom(innerClass)) {
                    continue;
                }
                if (ifaceClass != null) {
                    throw new IllegalStateException("Multiple Thrift Ifaces defined on handler");
                }
                ifaceClass = handlerInterfaceClass;
                processorClass = (Class<TProcessor>) innerClass;
                break;
            }
        }
        if (ifaceClass == null) {
            throw new IllegalStateException("No Thrift Ifaces found on handler");
        }

        handler = wrapHandler(ifaceClass, handler);

        Constructor<TProcessor> processorConstructor = processorClass.getConstructor(ifaceClass);

        TProcessor processor = BeanUtils.instantiateClass(processorConstructor, handler);

        AunicornServlet servlet = new AunicornServlet(processor);

        String servletBeanName = ifaceClass.getDeclaringClass().getSimpleName() + "Servlet";

        ServletRegistration.Dynamic registration = servletContext.addServlet(servletBeanName, servlet);

        if(url != null) {
            registration.addMapping(url);
        } else {
            registration.addMapping("/" + serviceClass.getSimpleName());
        }
    }

    private Object wrapHandler(Class<?> ifaceClass, Object handler) {
        ProxyFactory proxyFactory = new ProxyFactory(ifaceClass, new SingletonTargetSource(handler));
        proxyFactory.setFrozen(true);
        return proxyFactory.getProxy();
    }
}
