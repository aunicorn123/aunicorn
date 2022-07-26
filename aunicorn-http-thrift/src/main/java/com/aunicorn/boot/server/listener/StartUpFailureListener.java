package com.aunicorn.boot.server.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;

public class StartUpFailureListener implements ApplicationListener<ApplicationFailedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(StartUpFailureListener.class);

    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        logger.error("***********application start failure,process exit**********", event.getException());
        System.exit(1);
    }
}
