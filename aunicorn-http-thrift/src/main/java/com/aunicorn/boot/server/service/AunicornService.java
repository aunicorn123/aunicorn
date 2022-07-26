package com.aunicorn.boot.server.service;

import org.springframework.stereotype.Component;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.TYPE})
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Component
public @interface AunicornService {

    /**
     * AunicornService 接口url
     * @return
     */
    String value() default "";
}

